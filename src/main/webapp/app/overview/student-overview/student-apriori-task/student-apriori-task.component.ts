import { ITaskModel } from 'app/overview/tasks/task.model';
import { TasksService } from 'app/overview/tasks/tasks.service';
import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountService } from 'app/core/auth/account.service';
import { StudentService } from 'app/overview/shared/students/student-service';
import { ICourseInstanceInformationDTO } from 'app/overview/shared/students/students.model';
import { Subscription } from 'rxjs';
import * as CryptoJS from 'crypto-js';
import { aprioriConfig } from 'app/app.constants';

/**
 * Component for displaying students' apriori task.
 */
@Component({
  selector: 'jhi-apriori-student-task',
  templateUrl: './student-apriori-task.component.html',
  styleUrls: ['./student-apriori-task.component.scss'],
})
export class StudentAprioriTaskComponent implements OnInit {
  @Input() courseInstance: string | undefined;
  @Input() taskDifficultyId: string | undefined;

  public courses: ICourseInstanceInformationDTO[] = [];
  public loginName = '';

  public accId = String(this.accountService.getLoginName());
  public uuid = '';
  public readonly _instance?: ICourseInstanceInformationDTO;
  public _exerciseSheetUUID = '';
  public _taskUUID = '';
  public _taskNo = 0;
  public maxPoints = 0;
  public maxPointsI = 0;

  public _taskModelA?: ITaskModel | undefined;
  public set taskModelA(value: ITaskModel | undefined) {
    this._taskModelA = value;
  }
  public get taskModelA(): ITaskModel | undefined {
    return this._taskModelA;
  }
  public tas = this.taskModelA?.taskAssignmentTypeId;
  private _paramMapSubscription?: Subscription;

  /**
   * Constructor.
   *
   * @param
   */
  constructor(
    private activatedRoute: ActivatedRoute,
    private taskService: TasksService,
    private accountService: AccountService,
    private studentService: StudentService,
    private router: Router
  ) {
    const nav = this.router.getCurrentNavigation();
    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
    } else {
      this._exerciseSheetUUID = '../';
    }
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this._exerciseSheetUUID = paramMap.get('exerciseSheetUUID')!;
      this._taskUUID = paramMap.get('taskUUID')!;
      this._taskNo = Number(paramMap.get('taskNo')!);
      (async () => {
        const result = await this.taskService.getTaskAssignmentById(this._taskUUID, true).toPromise();
        this._taskModelA = result.body!;
      })();
    });
    this.studentService.getCourseInstancesOfLoggedInStudent().subscribe(values => {
      this.courses = values;
    });
    this.loginName = this.accountService.getLoginName()!;
  }

  /**
   * for creating initial parameter for exercise link
   */
  public start(): void {
    const maxPointsS = String(this._taskModelA?.maxPoints);
    const taskConfigIdS = String(this._taskModelA?.aprioriDatasetId);
    const taskNoS = Number(this._taskNo);
    const userIdS = String(this.accId);
    const courseInstanceS = String(this.courseInstance);
    const exerciseSheetUUIDS = String(this._exerciseSheetUUID);
    const difficultyLevelS = String(this.taskDifficultyId);
    const courseIndex = courseInstanceS.indexOf('#');
    const courseInstanceMod = courseInstanceS.substring(courseIndex + 1, courseInstanceS.length);
    const levelIndex = difficultyLevelS.indexOf('#');
    const difficultyLevelMod = difficultyLevelS.substring(levelIndex + 1, difficultyLevelS.length);
    const delimiter = String('[[{}]]');
    const linkString = String(
      userIdS +
        delimiter +
        courseInstanceMod +
        delimiter +
        exerciseSheetUUIDS +
        delimiter +
        taskNoS.toString() +
        delimiter +
        difficultyLevelMod +
        delimiter +
        maxPointsS.toString() +
        delimiter +
        taskConfigIdS
    );
    const key = CryptoJS.enc.Utf8.parse(aprioriConfig.key);
    const ciphertext = CryptoJS.AES.encrypt(linkString, key, { iv: key }).toString();
    const b64enc = btoa(ciphertext);
    const standChar = b64enc.replace('/', '-');
    const linkExercise = aprioriConfig.baseUrl + aprioriConfig.baseExercise + standChar;
    window.open(linkExercise, '_blank');
  }

  /**
   * for starting apriori exercise
   */
  public startExercise(): void {
    this.start();
  }

  /**
   * for creating initial parameter for training link
   */
  public training(): void {
    const taskConfigIdS = String(this._taskModelA?.aprioriDatasetId);
    const courseInstanceS = String(this.courseInstance);
    const exerciseSheetUUIDS = String(this._exerciseSheetUUID);
    const delimiter = String('[[{}]]');
    const courseIndex = courseInstanceS.indexOf('#');
    const courseInstanceMod = courseInstanceS.substring(courseIndex + 1, courseInstanceS.length);
    const linkString = String(courseInstanceMod + delimiter + exerciseSheetUUIDS + delimiter + taskConfigIdS);
    const key = CryptoJS.enc.Utf8.parse(aprioriConfig.key);
    const ciphertext = CryptoJS.AES.encrypt(linkString, key, { iv: key }).toString();
    const b64enc = btoa(ciphertext);
    const standChar = b64enc.replace('/', '-');
    const linkTraining = aprioriConfig.baseUrl + aprioriConfig.baseTraining + standChar;
    window.open(linkTraining, '_blank');
  }

  /**
   * for starting apriori training
   */
  public startTraining(): void {
    this.training();
  }
}
