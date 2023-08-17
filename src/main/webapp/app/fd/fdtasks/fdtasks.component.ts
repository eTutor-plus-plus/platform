import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {MonacoEditorModule} from "ngx-monaco-editor-v2";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, Validators} from "@angular/forms";
import {RxReactiveFormsModule} from "@rxweb/reactive-form-validators";
import {SharedLibsModule} from "../../shared/shared-libs.module";
import {SharedModule} from "../../shared/shared.module";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'jhi-fdtasks',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule, MonacoEditorModule, NgbTooltipModule, ReactiveFormsModule, RxReactiveFormsModule, SharedLibsModule, SharedModule, TranslateModule],
  template: `
    <label class="form-control-label" for="fDependencies" jhiTranslate="taskManagement.taskGroup.update.fDependencies"
    >Functional Dependencies</label
    >
    <fa-icon
      icon="info-circle"
      ngbTooltip="{{ 'taskManagement.taskGroup.update.fDependenciesInfo' | translate }}"
      placement="right"
    ></fa-icon>
    <ngx-monaco-editor [options]="editorOptionsFD" id="fDependencies" formControlName="fDependencies"></ngx-monaco-editor>
  `,
  styleUrls: ['./fdtasks.component.scss']
})
export class FdtasksComponent implements OnInit {
  public editorOptionsFD = { theme: 'vs-light', language: 'fd' };


  constructor(


  ) {}

  ngOnInit(): void {

  }

}
