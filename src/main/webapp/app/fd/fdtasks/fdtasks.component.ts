import {Component, Input, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {MonacoEditorModule} from "ngx-monaco-editor-v2";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {FormArray, FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {RxReactiveFormsModule} from "@rxweb/reactive-form-validators";
import {SharedLibsModule} from "../../shared/shared-libs.module";
import {SharedModule} from "../../shared/shared.module";
import {TranslateModule} from "@ngx-translate/core";
import {ITaskGroupDTO} from "../../overview/tasks/tasks-overview/task-group-management/task-group-management.model";
import {FDModel} from "../FDModel";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Normalform} from "./normalform";
import {FDHint, FDTaskSolve, FDTaskSolveResponse, Solve} from "./FDTaskSolve";


@Component({
  selector: 'jhi-fdtasks',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule, MonacoEditorModule, NgbTooltipModule, ReactiveFormsModule, RxReactiveFormsModule, SharedLibsModule, SharedModule, TranslateModule],
  template: `
    <h5>
      Aufgabenstellung
    </h5>
    <p *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#MinimalCover'">
      {{'fDAssignment.assignment.minimalCoverTaskDescription' | translate}}
    </p>
    <p *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Closure'">
      {{'fDAssignment.assignment.closureTaskDescription' | translate}}
    </p>
    <p *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Key'">
      {{'fDAssignment.assignment.keyTaskDescription' | translate}}
    </p>
    <p *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Normalform'">
      {{'fDAssignment.assignment.normalformTaskDescription' | translate}}
    </p>
    <p *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Normalization'">
      {{'fDAssignment.assignment.normalizeBCNFTaskDescription' | translate}}
    </p>
    <form [formGroup]="solveFdTaskForm">
      <div class="row">
        <div class="col-8">
          <dl class="row-md task-details">
            <dt>R</dt>
            <dd>
              <div class="row">
                <div class="col-2">{{fDExercise?.attributes}}</div>
                <div class="col-5" *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Normalform'">
                  <select [formControl]="solveFdTaskForm.controls.solution">
                    <option value="">{{ "fDAssignment.normalform.placeholder" | translate }}</option>
                    <option *ngFor="let normalform of normalforms"
                            [value]="normalform.value">{{ normalform.text | translate }}</option>
                  </select>
                </div>
              </div>
              <ng-container *ngFor="let hint of hints">
                <div class="row error"
                     *ngIf="hint.subId == null && solveFdTaskForm.invalid && fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Normalform'">
                  {{hint.hint}}
                </div>
              </ng-container>
            </dd>
            <dt>F</dt>
            <dd>
              <ng-container class="container"
                            *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Normalform'; else normal ">

                <ng-container *ngFor="let dependency of solveFdTaskForm.controls.normalForm.controls">
                  <div class="row">
                    <div class="col-2">{{dependency.controls.text.value}}</div>
                    <div class="col-5">
                      <select [formControl]="dependency.controls.solution">
                        <option value="">{{ "fDAssignment.normalform.none" | translate }}</option>
                        <option *ngFor="let normalform of normalforms"
                                [value]="normalform.value">{{ normalform.text | translate }}</option>
                      </select>
                    </div>
                  </div>
                  <ng-container *ngIf="solveFdTaskForm.invalid">
                    <ng-container *ngFor="let hint of hints">
                      <div class="row error" *ngIf="hint.subId === dependency.value.id">
                        {{hint.hint}}
                      </div>
                    </ng-container>
                  </ng-container>
                </ng-container>
              </ng-container>
              <ng-template #normal>
                <div class="row" *ngFor="let dependency of fDExercise?.functionalDependencies">
                  <div class="col-md-auto">{{dependency.leftSide}} → {{dependency.rightSide}}</div>
                </div>
              </ng-template>
            </dd>
          </dl>
          <ng-container class="container" class="table"
                        *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Closure'">
            <div class="fw-bold">A</div>
            <ng-container *ngFor="let hint of hints">
              <div class="error row" *ngIf="hint.subId == null">
                {{hint.hint | translate}}
              </div>
            </ng-container>
            <ng-container *ngFor="let element of solveFdTaskForm.controls.closure.controls">
              <div class="row">
                <div class="col-2">
                  {{element.controls.text.value}}
                </div>
                <div class="col-5">
                  <input [formControl]="element.controls.solution">
                </div>
              </div>
              <ng-container *ngIf="solveFdTaskForm.invalid">
                <ng-container *ngFor="let hint of hints">
                  <div class="row error" *ngIf="hint.subId == element.value.id">
                    {{hint.hint | translate}}
                  </div>
                </ng-container>
              </ng-container>
            </ng-container>
          </ng-container>
          <ng-container *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Key' ||
                                fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#MinimalCover'">
            <ng-container *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Key'">
              <label class="form-control-label" for="assignment" jhiTranslate="fDAssignment.info.key"
              ></label>
              <fa-icon
                icon="info-circle"
                ngbTooltip="{{ taskGroup?.taskGroupTypeId }}"
                placement="right"
              ></fa-icon>
            </ng-container>
            <ng-container *ngIf="fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#MinimalCover'">
              <label class="form-control-label" for="assignment" jhiTranslate="fDAssignment.info.minimalCover"
              ></label>
              <fa-icon
                icon="info-circle"
                ngbTooltip="minimal"
                placement="right"
              ></fa-icon>
            </ng-container>
            <ngx-monaco-editor [options]="editorOptionsFD"
                               [formControl]="solveFdTaskForm.controls.solution">
            </ngx-monaco-editor>
            <ng-container *ngIf="solveFdTaskForm.invalid">
              <div *ngFor="let hint of hints" class="error">
                {{ hint.hint | translate : {line: hint.subId} }}
              </div>
            </ng-container>
          </ng-container>
        </div>
      </div>
      <div>
        <form class="add-form" (ngSubmit)="submitDiagnose()">
          <input type="submit" value="Diagnose" class="btn btn-info"/>
        </form>
      </div>
      <div>
        <form class="add-form" (ngSubmit)="submitGrade()">
          <input type="submit" value="Submit" id="submit" class="btn btn-primary"/>
        </form>
      </div>

    </form>
  `,
  styleUrls: ['./fdtasks.component.scss']
})
export class FdtasksComponent implements OnInit {
  public editorOptionsFD = { theme: 'vs-light'};
  @Input() public taskGroup: ITaskGroupDTO | undefined;
  @Input() public exercise_id: string | undefined;
  @Input() public fDSubtype: string | undefined;
  public taskGroupId: string = '';
  public fDExercise: FDModel | undefined ;
  public solved: boolean = false;
  public readonly normalforms = Normalform.Values;
  public solveFdTaskForm = new FormGroup({
    solution: new FormControl(''),
    closure: new FormArray<FormGroup<{
      id: FormControl<string | null>,
      text: FormControl<string | null>,
      solution: FormControl<string | null>}>>([
    ]),
    normalForm: new FormArray<FormGroup<{
      id: FormControl<string | null>,
      text: FormControl<string | null>,
      solution: FormControl<string | null>}>>([
    ])
  });
  public hints: FDHint[] | null | undefined;

  constructor(
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.taskGroupId = this.taskGroup!.id.substring(this.taskGroup!.id.lastIndexOf("-")+1)
    this.getFDGroup(this.taskGroupId).subscribe(response =>  {
      this.fDExercise = response
      if (this.fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Normalform' &&
        this.fDExercise?.functionalDependencies !== undefined) {
        const normalformForm = this.solveFdTaskForm.controls.normalForm
        for (let dependency of this.fDExercise.functionalDependencies) {
          normalformForm.push(new FormGroup({id: new FormControl(dependency.id),
            text: new FormControl(dependency.leftSide.toString() +" → "+ dependency.rightSide.toString()),
            solution: new FormControl('')}))
        }
      }
    })
    ;
    if (this.fDSubtype === 'http://www.dke.uni-linz.ac.at/etutorpp/FDSubtype#Closure' && this.exercise_id !== undefined) {
      let id = this.exercise_id?.replace("Closure-", "")
      const closureForm = this.solveFdTaskForm.controls.closure
      this.getLeftSidesClosure(id).subscribe(response => Object.entries(response).forEach(
        function fillClosureForm(entry: [string,string[]]){
          closureForm.push(new FormGroup({
            id: new FormControl(entry[0]),
            text: new FormControl(entry[1].toString()),
            solution: new FormControl<string>('')
          }))
        }));
    }
  }

  public getFDGroup(id: string): Observable<FDModel> {
    return this.http.get<FDModel>(`/api/task-group/fd-solve/${id}`)
  }

  public getLeftSidesClosure(closureGroupId: string): Observable<Map<string, string>> {
    return this.http.get<Map<string, string>>(`/api/tasks/assignments/fd/closure/${closureGroupId}`)
  }

  submitDiagnose() {
    const fdSolve = this.buildFDTaskSolve()
    this.getTaskSolveResponse(fdSolve).subscribe(response => {
      this.solved = response.solved
      this.hints = response.hint
      if (!(response.solved)) {
        this.solveFdTaskForm.setErrors({"incorrect": true})
      } else {
        document.getElementById("submit")?.classList.add("btn-success")
        document.getElementById("submit")?.classList.remove("btn-primary");
      }
    })
  }
  public getTaskSolveResponse(fdTaskSolve: FDTaskSolve): Observable<FDTaskSolveResponse> {
    return this.http.post<FDTaskSolveResponse>(`/api/tasks/assignments/fd/solve`, fdTaskSolve)
  }

  submitGrade() {
    const fdTaskSolve = this.buildFDTaskSolve()
    this.http.post<FDTaskSolveResponse>(`/api/tasks/assignments/fd/grade`, fdTaskSolve)
  }
  private buildFDTaskSolve(): FDTaskSolve {
    const fdSolve: FDTaskSolve = {
      id: this.exercise_id,
      type: this.fDSubtype
    };
    if (this.solveFdTaskForm.controls.solution.value) {
      fdSolve.solution = this.solveFdTaskForm.controls.solution.value
    }

    if (this.solveFdTaskForm.value.closure && this.solveFdTaskForm.value.closure.length > 0) {
      fdSolve.closureSolutions = []
      for (let value of this.solveFdTaskForm.value.closure?.values()) {
        if (value.id) {
          fdSolve.closureSolutions?.push({id: value.id, solution: value.solution})
        }
      }
    }
    if (this.solveFdTaskForm.value.normalForm && this.solveFdTaskForm.value.normalForm.length > 0) {
      fdSolve.normalFormSolutions = []
      for (let value of this.solveFdTaskForm.value.normalForm?.values()) {
        if (value.id) {
          fdSolve.normalFormSolutions?.push({id: value.id,solution: value.solution})
        }
      }
    }
    return fdSolve;
  }
}


