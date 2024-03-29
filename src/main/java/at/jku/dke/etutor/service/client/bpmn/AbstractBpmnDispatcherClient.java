package at.jku.dke.etutor.service.client.bpmn;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.client.AbstractClient;

public abstract sealed class AbstractBpmnDispatcherClient extends AbstractClient permits
    BpmnSubmissionClient,
    BpmnClient {
    protected AbstractBpmnDispatcherClient(ApplicationProperties properties){
        super(properties.getBpmnDispatcher().getUrl());
    }
}
