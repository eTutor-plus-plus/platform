package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;

public abstract sealed class AbstractBpmnDispatcherProxyService extends AbstractProxyService permits
    BpmnSubmissionProxyService,
    BpmnProxyService {
    protected AbstractBpmnDispatcherProxyService(ApplicationProperties properties){
        super(properties.getBpmnDispatcher().getUrl());
    }
}
