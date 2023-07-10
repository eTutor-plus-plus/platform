import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FdtasksComponent } from './fdtasks.component';

describe('FdtasksComponent', () => {
  let component: FdtasksComponent;
  let fixture: ComponentFixture<FdtasksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FdtasksComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FdtasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
