import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { XQueryService } from '../../dispatcher/services/xquery.service';

/**
 * Component that displays an XML-File inside a Monaco-Editor instance
 * and provides a download function for the file
 */
@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './xml-file.component.html',
  styleUrls: ['./xml-file.component.scss'],
  providers: [TranslatePipe],
})
export class XmlFileComponent implements OnInit, OnDestroy {
  public xml?: string;
  public editorOptions = { theme: 'vs-light', language: 'xml', readOnly: 'true' };

  private id!: string | null;
  private taskGroup?: string | null;
  private _paramMapSubscription?: Subscription;
  private _queryParamSubscription?: Subscription;

  /**
   * constructor
   * @param router the injected routing service
   * @param activatedRoute the injected activated route
   */
  constructor(private router: Router, private activatedRoute: ActivatedRoute, private service: XQueryService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(() => {
      this._queryParamSubscription = this.activatedRoute.queryParamMap.subscribe(queryParams => {
        this.taskGroup = queryParams.get('taskGroup');
        this.id = queryParams.get('id');
        this.xml = 'not fetched';
        (async () => {
          this.xml = await this.service.getXML(this.id!).toPromise();
        })();
      });
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  ngOnDestroy(): void {
    this._queryParamSubscription?.unsubscribe();
    this._paramMapSubscription?.unsubscribe();
  }

  /**
   * Downloads the XML-File
   */
  public download(): void {
    if (this.id) {
      this.service.downloadXML(this.id);
    }
  }
}
