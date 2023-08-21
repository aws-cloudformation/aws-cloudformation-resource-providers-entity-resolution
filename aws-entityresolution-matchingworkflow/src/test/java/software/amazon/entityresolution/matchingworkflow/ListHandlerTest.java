package software.amazon.entityresolution.matchingworkflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListMatchingWorkflowsRequest;
import software.amazon.awssdk.services.entityresolution.model.ListMatchingWorkflowsResponse;
import software.amazon.awssdk.services.entityresolution.model.MatchingWorkflowSummary;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.ThrottlingException;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    private static final Instant TIME = Instant.now();
    private static final String WORKFLOW_NAME_1 = "workflowName1";
    private static final String WORKFLOW_NAME_2 = "workflowName2";
    private static final String WORKFLOW_ARN_1 = "arn:aws:entityresolution:us-east-1:123456789012:matchingworkflow"
        + "/workflowName1";
    private static final String WORKFLOW_ARN_2 = "arn:aws:entityresolution:us-east-1:123456789012:matchingworkflow"
        + "/workflowName2";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private EntityResolutionClient client;

    @Mock
    private Logger logger;

    private static ResourceModel model;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        client = mock(EntityResolutionClient.class);
        logger = mock(Logger.class);

        model = ResourceModel.builder()
                             .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler(client);
        final MatchingWorkflowSummary listMatchingWorkflow1 = MatchingWorkflowSummary.builder()
                                                                                     .workflowName(WORKFLOW_NAME_1)
                                                                                     .workflowArn(WORKFLOW_ARN_1)
                                                                                     .createdAt(TIME)
                                                                                     .updatedAt(TIME)
                                                                                     .build();

        final MatchingWorkflowSummary listMatchingWorkflow2 = MatchingWorkflowSummary.builder()
                                                                                     .workflowName(WORKFLOW_NAME_2)
                                                                                     .workflowArn(WORKFLOW_ARN_2)
                                                                                     .createdAt(TIME)
                                                                                     .updatedAt(TIME)
                                                                                     .build();

        final ListMatchingWorkflowsResponse listMatchingWorkflowsResponse = ListMatchingWorkflowsResponse.builder()
                                                                                                         .workflowSummaries(
                                                                                                             Lists.newArrayList(
                                                                                                                 listMatchingWorkflow1,
                                                                                                                 listMatchingWorkflow2))
                                                                                                         .build();

        Mockito.doReturn(listMatchingWorkflowsResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListMatchingWorkflowsRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()
                           .get(0)
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME_1);
        assertThat(response.getResourceModels()
                           .get(1)
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME_2);
        assertThat(response.getResourceModels()
                           .get(0)
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN_1);
        assertThat(response.getResourceModels()
                           .get(1)
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN_2);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_throwsAccessDeniedException() {
        final ListHandler handler = new ListHandler(client);
        AccessDeniedException exception = AccessDeniedException.builder()
                                                               .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListMatchingWorkflowsRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnAccessDeniedException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsResourceNotFoundException() {
        final ListHandler handler = new ListHandler(client);
        ResourceNotFoundException exception = ResourceNotFoundException.builder()
                                                                       .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListMatchingWorkflowsRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsInternalServerException() {
        final ListHandler handler = new ListHandler(client);
        InternalServerException exception = InternalServerException.builder()
                                                                   .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListMatchingWorkflowsRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnServiceInternalErrorException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsValidationException() {
        final ListHandler handler = new ListHandler(client);
        ValidationException exception = ValidationException.builder()
                                                           .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListMatchingWorkflowsRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnInvalidRequestException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsOtherException() {
        final ListHandler handler = new ListHandler(client);
        ThrottlingException exception = ThrottlingException.builder()
                                                           .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListMatchingWorkflowsRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnGeneralServiceException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
