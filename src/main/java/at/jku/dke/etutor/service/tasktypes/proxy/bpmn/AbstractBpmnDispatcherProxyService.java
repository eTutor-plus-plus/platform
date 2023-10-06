package at.jku.dke.etutor.service.tasktypes.proxy.bpmn;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.tasktypes.proxy.AbstractProxyService;

public abstract sealed class AbstractBpmnDispatcherProxyService extends AbstractProxyService permits
    BpmnSubmissionProxyService,
    BpmnProxyService {
    protected AbstractBpmnDispatcherProxyService(ApplicationProperties properties){
        super(properties.getBpmnDispatcher().getUrl());
    }
}
