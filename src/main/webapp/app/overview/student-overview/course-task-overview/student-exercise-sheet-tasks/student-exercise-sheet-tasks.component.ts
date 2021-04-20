import { Component, OnDestroy, OnInit } from '@angular/core';
import { StudentService } from '../../../shared/students/student-service';
import { ActivatedRoute, Router } from '@angular/router';
import { ICourseInstanceInformationDTO } from '../../../shared/students/students.model';
import { Subscription } from 'rxjs';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component which is used for displaying the individual tasks
 * of a student's exercise sheet.
 */
@Component({
  selector: 'jhi-student-exercise-sheet-tasks',
  templateUrl: './student-exercise-sheet-tasks.component.html',
  styleUrls: ['./student-exercise-sheet-tasks.component.scss'],
})
export class StudentExerciseSheetTasksComponent implements OnInit, OnDestroy {
  private _instance?: ICourseInstanceInformationDTO;
  private _paramMapSubscription?: Subscription;
  private _exerciseSheetUUIDId = '';

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected router
   * @param location the injected location service
   * @param activatedRoute the injected activated route
   */
  constructor(
    private studentService: StudentService,
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute
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
      this._exerciseSheetUUIDId = paramMap.get('exerciseSheetUUID')!;
      this.loadTasksAsync();
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    this._paramMapSubscription?.unsubscribe();
  }
  /* eslint-disable @typescript-eslint/require-await */
  /**
   * Asynchronously loads the student's tasks.
   */
  private async loadTasksAsync(): Promise<any> {
    // TODO: Implement

    return null!;
  }
}
