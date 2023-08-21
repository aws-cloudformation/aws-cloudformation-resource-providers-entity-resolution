package software.amazon.entityresolution.matchingworkflow;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.TagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.UntagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.UpdateMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.UpdateMatchingWorkflowResponse;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@NoArgsConstructor
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private EntityResolutionClient client;

    public UpdateHandler(EntityResolutionClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel requestModel = request.getDesiredResourceState();

        final Set<Tag> previousTags = request.getPreviousResourceTags() == null ? new HashSet<>()
            : Translator.mapTagsToSet(request.getPreviousResourceTags());

        if (previousTags != null) {
            final List<String> tagsToRemove = previousTags.stream()
                                                          .map(Tag::getKey)
                                                          .collect(Collectors.toList());

            if (tagsToRemove.size() > 0) {
                final UntagResourceRequest untagResourceRequest = UntagResourceRequest.builder()
                                                                                      .resourceArn(
                                                                                          Translator.toWorkflowArn(
                                                                                              request))
                                                                                      .tagKeys(tagsToRemove)
                                                                                      .build();

                proxy.injectCredentialsAndInvokeV2(untagResourceRequest, client::untagResource);
            }
        }

        if (request.getDesiredResourceTags() != null && !request.getDesiredResourceTags()
                                                                .isEmpty()) {
            final Map<String, String> resourceTags = request.getDesiredResourceTags();

            final TagResourceRequest tagResourceRequest = TagResourceRequest.builder()
                                                                            .resourceArn(
                                                                                Translator.toWorkflowArn(request))
                                                                            .tags(resourceTags)
                                                                            .build();

            proxy.injectCredentialsAndInvokeV2(tagResourceRequest, client::tagResource);
        }

        final UpdateMatchingWorkflowRequest updateMatchingWorkflowRequest = UpdateMatchingWorkflowRequest.builder()
                                                                                                         .workflowName(
                                                                                                             requestModel.getWorkflowName())
                                                                                                         .description(
                                                                                                             requestModel.getDescription())
                                                                                                         .inputSourceConfig(
                                                                                                             Translator.translateToVeniceInputSourceConfig(
                                                                                                                 requestModel.getInputSourceConfig()))
                                                                                                         .outputSourceConfig(
                                                                                                             Translator.translateToVeniceOutputSourceConfig(
                                                                                                                 requestModel.getOutputSourceConfig()))
                                                                                                         .resolutionTechniques(
                                                                                                             Translator.translateToVeniceResolutionTechniques(
                                                                                                                 requestModel.getResolutionTechniques()))
                                                                                                         .roleArn(
                                                                                                             requestModel.getRoleArn())
                                                                                                         .build();

        final ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
                                                                                                .resourceArn(
                                                                                                    Translator.toWorkflowArn(
                                                                                                        request))
                                                                                                .build();
        final UpdateMatchingWorkflowResponse updateMatchingWorkflowResponse;

        final ListTagsForResourceResponse listTagsForResourceResponse;

        try {
            updateMatchingWorkflowResponse = proxy.injectCredentialsAndInvokeV2(updateMatchingWorkflowRequest,
                client::updateMatchingWorkflow);
            logger.log(
                String.format("Updated Matching Workflow with workflowName = %s", requestModel.getWorkflowName()));

            listTagsForResourceResponse = proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest,
                client::listTagsForResource);
            logger.log(String.format("Retrieve Tags for workflowName = %s", requestModel.getWorkflowName()));
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(e);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final Exception e) {
            throw new CfnGeneralServiceException(e);
        }

        final ResourceModel responseModel = ResourceModel.builder()
                                                         .description(updateMatchingWorkflowResponse.description())
                                                         .inputSourceConfig(
                                                             Translator.translateToCfnInputSourceConfig(
                                                                 updateMatchingWorkflowResponse.inputSourceConfig(),
                                                                 Translator.toWorkflowArn(
                                                                     request)))
                                                         .outputSourceConfig(
                                                             Translator.translateToCfnOutputSourceConfig(
                                                                 updateMatchingWorkflowResponse.outputSourceConfig()))
                                                         .resolutionTechniques(
                                                             Translator.translateToCfnResolutionTechniques(
                                                                 updateMatchingWorkflowResponse.resolutionTechniques()))
                                                         .roleArn(updateMatchingWorkflowResponse.roleArn())
                                                         .workflowName(updateMatchingWorkflowResponse.workflowName())
                                                         .tags(Translator.mapTagsToSet(
                                                             listTagsForResourceResponse.tags()))
                                                         .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }
}
