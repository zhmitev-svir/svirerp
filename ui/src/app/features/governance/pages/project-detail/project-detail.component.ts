import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';

import { ProjectService } from '../../services/project.service';
import { ProjectTaskService } from '../../services/project-task.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Project, ProjectTask, ProjectComment, ProjectTaskComment } from '../../../../core/models/domain.model';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ProjectFormComponent } from '../project-form/project-form.component';
import { ProjectTaskFormComponent } from '../project-task-form/project-task-form.component';

const PROJECT_STATUS_LABELS: Record<string, string> = {
  planning: 'Planning',
  in_progress: 'In Progress',
  on_hold: 'On Hold',
  completed: 'Completed',
  cancelled: 'Cancelled',
};

const TASK_STATUS_LABELS: Record<string, string> = {
  todo: 'To Do',
  in_progress: 'In Progress',
  blocked: 'Blocked',
  done: 'Done',
};

function formatDateTime(iso?: string): string {
  return iso ? new Date(iso).toLocaleString() : '';
}

/**
 * Cards, not a wide `<table>`, for Tasks — deliberately, so this page stays usable on a narrow
 * mobile viewport (a multi-column table is the pattern MeetingMinutesDetailComponent uses for
 * Action Items, but that one isn't mobile-friendly; this page must be, per the request that added
 * it). Each task's comment thread is a collapsed-by-default `mat-expansion-panel` — lazy-loaded on
 * first open — rather than a separate routed page, so drilling into a task's discussion never
 * costs a full navigation on a phone.
 */
@Component({
  selector: 'app-project-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    PageHeaderComponent,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
  ],
  template: `
    @if (project(); as p) {
      <div class="page-container">
        <app-page-header [title]="p.name" [subtitle]="statusLabels[p.status]">
          <ng-container extraActions>
            <button mat-stroked-button (click)="editProject()">
              <mat-icon>edit</mat-icon>
              Edit
            </button>
          </ng-container>
        </app-page-header>

        <mat-card class="summary-card">
          <mat-card-content>
            @if (p.description) {
              <p class="description">{{ p.description }}</p>
            }
            <div class="meta-row">
              <span class="meta-item">
                <mat-icon inline>person</mat-icon>
                {{ p.assignee ? (p.assignee.firstName + ' ' + p.assignee.lastName) : 'Unassigned' }}
              </span>
              @if (p.dueDate) {
                <span class="meta-item">
                  <mat-icon inline>event</mat-icon>
                  Due {{ p.dueDate }}
                </span>
              }
            </div>
          </mat-card-content>
        </mat-card>

        <div class="section-header">
          <h2 class="mat-headline-6">Tasks</h2>
          <button mat-stroked-button (click)="addTask()">
            <mat-icon>add</mat-icon>
            Add Task
          </button>
        </div>

        @for (task of tasks(); track task.id) {
          <mat-card class="task-card">
            <mat-card-content>
              <div class="task-header">
                <span class="task-name">{{ task.name }}</span>
                <span class="status-chip" [attr.data-status]="task.status">{{ taskStatusLabels[task.status] }}</span>
              </div>
              @if (task.description) {
                <p class="description">{{ task.description }}</p>
              }
              <div class="meta-row">
                <span class="meta-item">
                  <mat-icon inline>person</mat-icon>
                  {{ task.assignee ? (task.assignee.firstName + ' ' + task.assignee.lastName) : 'Unassigned' }}
                </span>
              </div>
              <div class="task-actions">
                <button mat-button (click)="editTask(task)">
                  <mat-icon>edit</mat-icon>
                  Edit
                </button>
                <button mat-button (click)="confirmDeleteTask(task)">
                  <mat-icon>delete</mat-icon>
                  Delete
                </button>
              </div>
            </mat-card-content>

            <mat-expansion-panel (opened)="loadTaskComments(task.id)">
              <mat-expansion-panel-header>
                Comments @if (taskCommentCount(task.id) !== null) { ({{ taskCommentCount(task.id) }}) }
              </mat-expansion-panel-header>

              <div class="comment-thread">
                @for (c of taskComments()[task.id] ?? []; track c.id) {
                  <div class="comment">
                    <div class="comment-meta">
                      <span class="comment-author">{{ c.authorName }}</span>
                      <span class="comment-date">{{ formatDateTime(c.createdAt) }}</span>
                    </div>
                    <p class="comment-text">{{ c.comment }}</p>
                  </div>
                } @empty {
                  <p class="empty">No comments yet.</p>
                }
              </div>

              <div class="add-comment">
                <mat-form-field appearance="outline" class="full-width no-hint">
                  <textarea matInput rows="2" placeholder="Add a comment…"
                      [(ngModel)]="newTaskComment[task.id]"></textarea>
                </mat-form-field>
                <button mat-flat-button color="primary"
                        [disabled]="!newTaskComment[task.id]?.trim() || postingTaskComment[task.id]"
                        (click)="addTaskComment(task.id)">
                  Post
                </button>
              </div>
            </mat-expansion-panel>
          </mat-card>
        } @empty {
          <p class="empty">No tasks yet.</p>
        }

        <div class="section-header">
          <h2 class="mat-headline-6">Comments</h2>
        </div>

        <mat-card class="comment-thread-card">
          <mat-card-content>
            <div class="comment-thread">
              @for (c of comments(); track c.id) {
                <div class="comment">
                  <div class="comment-meta">
                    <span class="comment-author">{{ c.authorName }}</span>
                    <span class="comment-date">{{ formatDateTime(c.createdAt) }}</span>
                  </div>
                  <p class="comment-text">{{ c.comment }}</p>
                </div>
              } @empty {
                <p class="empty">No comments yet.</p>
              }
            </div>

            <div class="add-comment">
              <mat-form-field appearance="outline" class="full-width no-hint">
                <textarea matInput rows="2" placeholder="Add a comment…" [(ngModel)]="newComment"></textarea>
              </mat-form-field>
              <button mat-flat-button color="primary"
                      [disabled]="!newComment.trim() || postingComment()"
                      (click)="addComment()">
                Post
              </button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    }
  `,
  styles: [`
    .summary-card, .task-card, .comment-thread-card { margin-bottom: 16px; }
    .description { white-space: pre-wrap; margin: 0 0 12px; }
    .meta-row { display: flex; flex-wrap: wrap; gap: 16px; color: rgba(0,0,0,.6); font-size: .9em; }
    .meta-item { display: flex; align-items: center; gap: 4px; }
    .section-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; margin: 8px 0 12px; }
    .section-header h2 { margin: 0; }

    .task-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
    .task-name { font-weight: 500; font-size: 1.05em; }
    .status-chip {
      display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: .78em;
      background: rgba(0,0,0,.08); white-space: nowrap;
    }
    .status-chip[data-status="done"], .status-chip[data-status="completed"] { background: #c8e6c9; }
    .status-chip[data-status="blocked"], .status-chip[data-status="cancelled"] { background: #ffcdd2; }
    .status-chip[data-status="in_progress"] { background: #bbdefb; }
    .status-chip[data-status="on_hold"] { background: #ffe0b2; }
    .task-actions { display: flex; flex-wrap: wrap; gap: 4px; margin: 4px 0 8px; }

    .comment-thread { display: flex; flex-direction: column; gap: 12px; padding: 8px 0; }
    .comment { border-left: 3px solid rgba(0,0,0,.1); padding-left: 10px; }
    .comment-meta { display: flex; flex-wrap: wrap; gap: 8px; font-size: .85em; color: rgba(0,0,0,.6); }
    .comment-author { font-weight: 500; }
    .comment-text { white-space: pre-wrap; margin: 4px 0 0; }
    .empty { color: rgba(0,0,0,.5); font-style: italic; margin: 8px 0; }

    .add-comment { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; padding-top: 4px; }
    .full-width { width: 100%; }
    .no-hint { margin-bottom: -1.25em; }
  `],
})
export class ProjectDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private projectService = inject(ProjectService);
  private taskService = inject(ProjectTaskService);
  private dialog = inject(MatDialog);
  private notifications = inject(NotificationService);

  private projectId = this.route.snapshot.paramMap.get('id')!;

  readonly statusLabels = PROJECT_STATUS_LABELS;
  readonly taskStatusLabels = TASK_STATUS_LABELS;
  formatDateTime = formatDateTime;

  project = signal<Project | null>(null);
  tasks = signal<ProjectTask[]>([]);
  comments = signal<ProjectComment[]>([]);
  // Explicitly `| undefined` per key — Record<string, T>'s index signature otherwise claims every
  // key is always present, which isn't true here until that task's comment panel has been opened
  // at least once (see loadTaskComments).
  taskComments = signal<Record<string, ProjectTaskComment[] | undefined>>({});

  newComment = '';
  postingComment = signal(false);
  newTaskComment: Record<string, string> = {};
  postingTaskComment: Record<string, boolean> = {};

  ngOnInit(): void {
    this.loadProject();
    this.loadTasks();
    this.loadComments();
  }

  taskCommentCount(taskId: string): number | null {
    const loaded = this.taskComments()[taskId];
    return loaded ? loaded.length : null;
  }

  editProject(): void {
    const p = this.project();
    if (!p) return;
    this.dialog
      .open(ProjectFormComponent, { width: '540px', data: { orgId: p.org.id, project: p } })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadProject(); });
  }

  addTask(): void {
    this.dialog
      .open(ProjectTaskFormComponent, {
        width: '540px',
        // Prefills the new task's assignee from the project's own assignee (still editable) —
        // see ProjectTaskFormComponent's defaultAssignee doc.
        data: { projectId: this.projectId, task: null, defaultAssignee: this.project()?.assignee ?? null },
      })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadTasks(); });
  }

  editTask(task: ProjectTask): void {
    this.dialog
      .open(ProjectTaskFormComponent, { width: '540px', data: { projectId: this.projectId, task } })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadTasks(); });
  }

  confirmDeleteTask(task: ProjectTask): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Delete Task',
          message: `Delete "${task.name}" and all of its comments? This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .subscribe(confirmed => { if (confirmed) this.deleteTask(task); });
  }

  loadTaskComments(taskId: string): void {
    // Lazy: only fetched the first time a task's comment panel is opened.
    if (this.taskComments()[taskId]) return;
    this.taskService.getComments(taskId).subscribe(list => {
      this.taskComments.update(map => ({ ...map, [taskId]: list }));
    });
  }

  addTaskComment(taskId: string): void {
    const text = this.newTaskComment[taskId]?.trim();
    if (!text) return;
    this.postingTaskComment[taskId] = true;
    this.taskService.addComment(taskId, text).subscribe({
      next: created => {
        this.taskComments.update(map => ({ ...map, [taskId]: [...(map[taskId] ?? []), created] }));
        this.newTaskComment[taskId] = '';
        this.postingTaskComment[taskId] = false;
      },
      error: () => { this.postingTaskComment[taskId] = false; },
    });
  }

  addComment(): void {
    const text = this.newComment.trim();
    if (!text) return;
    this.postingComment.set(true);
    this.projectService.addComment(this.projectId, text).subscribe({
      next: created => {
        this.comments.update(list => [...list, created]);
        this.newComment = '';
        this.postingComment.set(false);
      },
      error: () => this.postingComment.set(false),
    });
  }

  private loadProject(): void {
    this.projectService.getById(this.projectId).subscribe(p => this.project.set(p));
  }

  private loadTasks(): void {
    this.taskService.getForProject(this.projectId).subscribe(list => this.tasks.set(list));
  }

  private loadComments(): void {
    this.projectService.getComments(this.projectId).subscribe(list => this.comments.set(list));
  }

  private deleteTask(task: ProjectTask): void {
    this.taskService.remove(task.id).subscribe({
      next: () => {
        this.tasks.set(this.tasks().filter(t => t.id !== task.id));
        this.notifications.success('Task deleted.');
      },
    });
  }
}
