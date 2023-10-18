package software.amazon.entityresolution.schemamapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.ConflictException;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingResponse;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.TagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.UntagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.UpdateSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.UpdateSchemaMappingResponse;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
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

        final GetSchemaMappingRequest getSchemaMappingRequest = GetSchemaMappingRequest.builder()
                                                                                       .schemaName(
                                                                                           requestModel.getSchemaName())
                                                                                       .build();

        final GetSchemaMappingResponse getSchemaMappingResponse = getSchemaMapping(proxy, logger, requestModel,
            getSchemaMappingRequest);

        updateTagsForSchemaMapping(proxy, request);

        if (getSchemaMappingResponse.hasWorkflows()
                                    .equals(false)) {
            updateSchemaMapping(proxy, request, logger);
        }

        return new ReadHandler().handleRequest(proxy, request, callbackContext, logger);
    }

    private GetSchemaMappingResponse getSchemaMapping(
        final AmazonWebServicesClientProxy proxy,
        final Logger logger,
        final ResourceModel requestModel,
        final GetSchemaMappingRequest getSchemaMappingRequest) {

        final GetSchemaMappingResponse getSchemaMappingResponse;

        try {
            getSchemaMappingResponse = proxy.injectCredentialsAndInvokeV2(getSchemaMappingRequest,
                client::getSchemaMapping);
            logger.log(String.format("Retrieved Schema Mapping with schemaName = %s", requestModel.getSchemaName()));
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

        return getSchemaMappingResponse;
    }

    private void updateTagsForSchemaMapping(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request) {
        final Set<Tag> previousTags = request.getPreviousResourceTags() == null ? new HashSet<>()
            : Translator.mapTagsToSet(request.getPreviousResourceTags());

        if (previousTags != null) {
            final List<String> tagsToRemove = previousTags.stream()
                                                          .map(Tag::getKey)
                                                          .collect(Collectors.toList());

            if (tagsToRemove.size() > 0) {
                final UntagResourceRequest untagResourceRequest = UntagResourceRequest.builder()
                                                                                      .resourceArn(
                                                                                          Translator.toSchemaArn(
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
                                                                                Translator.toSchemaArn(request))
                                                                            .tags(resourceTags)
                                                                            .build();

            proxy.injectCredentialsAndInvokeV2(tagResourceRequest, client::tagResource);
        }
    }

    public ProgressEvent<ResourceModel, CallbackContext> updateSchemaMapping(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final Logger logger) {

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel requestModel = request.getDesiredResourceState();

        final UpdateSchemaMappingRequest updateSchemaMappingRequest = UpdateSchemaMappingRequest.builder()
                                                                                                .schemaName(
                                                                                                    requestModel.getSchemaName())
                                                                                                .description(
                                                                                                    requestModel.getDescription())
                                                                                                .mappedInputFields(
                                                                                                    Translator.translateFromInternalSchemaInputAttributes(
                                                                                                        requestModel.getMappedInputFields()))
                                                                                                .build();

        final ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
                                                                                                .resourceArn(
                                                                                                    Translator.toSchemaArn(
                                                                                                        request))
                                                                                                .build();
        final UpdateSchemaMappingResponse updateSchemaMappingResponse;

        final ListTagsForResourceResponse listTagsForResourceResponse;

        try {
            updateSchemaMappingResponse = proxy.injectCredentialsAndInvokeV2(updateSchemaMappingRequest,
                client::updateSchemaMapping);
            logger.log(
                String.format("Updated SchemaMapping with schemaName = %s", requestModel.getSchemaName()));

            listTagsForResourceResponse = proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest,
                client::listTagsForResource);
            logger.log(String.format("Retrieve Tags for schemaName = %s", requestModel.getSchemaName()));
        } catch (final ConflictException e) {
            throw new CfnInternalFailureException(e);
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
                                                         .description(updateSchemaMappingResponse.description())
                                                         .mappedInputFields(
                                                             Translator.translateToInternalSchemaInputAttributes(
                                                                 updateSchemaMappingResponse.mappedInputFields()))
                                                         .schemaName(updateSchemaMappingResponse.schemaName())
                                                         .tags(Translator.mapTagsToSet(
                                                             listTagsForResourceResponse.tags()))
                                                         .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }
}
