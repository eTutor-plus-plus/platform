import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { ICourseInstanceInformationDTO } from 'app/overview/shared/students/students.model';
import { Subscription } from 'rxjs';
import { TasksService } from 'app/overview/tasks/tasks.service';
import { ITaskModel, TaskDifficulty } from 'app/overview/tasks/task.model';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component for displaying students' tasks.
 */
@Component({
  selector: 'jhi-student-task',
  templateUrl: './student-task.component.html',
  styleUrls: ['./student-task.component.scss'],
})
export class StudentTaskComponent implements OnInit, OnDestroy {
  private readonly _instance?: ICourseInstanceInformationDTO;
  private _paramMapSubscription?: Subscription;
  private _exerciseSheetUUID = '';
  private _taskUUID = '';
  private _taskModel?: ITaskModel;

  /**
   * Constructor.
   *
   * @param router the injected router
   * @param location the injected location service
   * @param activatedRoute the injected activated route
   * @param taskService the injected task service
   */
  constructor(
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private taskService: TasksService
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
    } else {
      this.router.navigate(['/']);
    }
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this._exerciseSheetUUID = paramMap.get('exerciseSheetUUID')!;
      this._taskUUID = paramMap.get('taskUUID')!;

      (async () => {
        const result = await this.taskService.getTaskAssignmentById(this._taskUUID, true).toPromise();
        this._taskModel = result.body!;
      })();
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    this._paramMapSubscription?.unsubscribe();
  }

  /**
   * Returns the task model.
   */
  public get taskModel(): ITaskModel {
    return this._taskModel!;
  }

  /**
   * Navigates back.
   */
  public navigateBack(): void {
    this.location.back();
  }

  /**
   * Returns the i18n string of a given difficulty url.
   *
   * @param url the url
   */
  public getDifficultyI18nString(url: string): string {
    return TaskDifficulty.fromString(url)!.text;
  }
}
