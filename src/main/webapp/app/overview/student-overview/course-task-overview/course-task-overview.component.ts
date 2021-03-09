import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { StudentService } from '../../shared/students/student-service';
import { Router } from '@angular/router';
import { ICourseInstanceInformationDTO, ICourseInstanceProgressOverviewDTO } from '../../shared/students/students.model';
import { TaskDifficulty } from '../../tasks/task.model';

/**
 * Component for displaying the student's task of a given course instance.
 */
@Component({
  selector: 'jhi-course-task-overview',
  templateUrl: './course-task-overview.component.html',
  styleUrls: ['./course-task-overview.component.scss'],
})
export class CourseTaskOverviewComponent implements OnInit {
  public instance?: ICourseInstanceInformationDTO;
  public items: ICourseInstanceProgressOverviewDTO[] = [];

  /**
   * Constructor.
   *
   * @param studentService the injected student service
   * @param router the injected router
   * @param location the injected location service
   */
  constructor(private studentService: StudentService, private router: Router, private location: Location) {
    const nav = this.router.getCurrentNavigation();

    if (nav && nav.extras.state) {
      this.instance = nav.extras.state.instance;
    } else {
      this.router.navigate(['/']);
    }
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    if (this.instance) {
      this.studentService.getStudentCourseInstanceProgressOverview(this.instance.instanceId).subscribe(value => (this.items = value));
    }
  }

  /**
   * Returns the i18n string of a given difficulty url.
   *
   * @param url the url
   */
  public getDifficultyI18nString(url: string): string {
    return TaskDifficulty.fromString(url)!.text;
  }

  /**
   * Tracks the identity of the given item.
   *
   * @param index the index
   * @param item the item
   */
  public trackIdentity(index: number, item: ICourseInstanceProgressOverviewDTO): string {
    return item.exerciseSheetId;
  }

  /**
   * Navigates back.
   */
  public navigateBack(): void {
    this.location.back();
  }
}
