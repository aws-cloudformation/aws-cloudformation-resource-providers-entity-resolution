package software.amazon.entityresolution.schemamapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingResponse;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.TagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.UntagResourceRequest;
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

        final GetSchemaMappingRequest getSchemaMappingRequest = GetSchemaMappingRequest.builder()
                                                                                       .schemaName(
                                                                                           requestModel
                                                                                               .getSchemaName())
                                                                                       .build();

        getSchemaMapping(proxy, logger, requestModel, getSchemaMappingRequest);

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
}
