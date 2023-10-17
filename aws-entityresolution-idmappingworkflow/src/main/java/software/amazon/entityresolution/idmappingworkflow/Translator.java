package software.amazon.entityresolution.idmappingworkflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.entityresolution.model.IdMappingWorkflowInputSource;
import software.amazon.awssdk.services.entityresolution.model.IntermediateSourceConfiguration;
import software.amazon.awssdk.services.entityresolution.model.IdMappingWorkflowOutputSource;
import software.amazon.awssdk.services.entityresolution.model.IdMappingTechniques;
import software.amazon.awssdk.services.entityresolution.model.ProviderProperties;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * This class is a centralized placeholder for - api request construction - object translation to/from aws sdk -
 * resource model construction for read/list handlers
 */

public class Translator {

      public static String WORKFLOW_ARN_FORMAT = "arn:%s:entityresolution:%s:%s:idmappingworkflow/%s";

      public static List<IdMappingWorkflowInputSource> translateToVeniceInputSourceConfig(
              List<software.amazon.entityresolution.idmappingworkflow.IdMappingWorkflowInputSource> source) {
          if (source == null) {
          return null;
          }

          return source.stream()
                  .map(inputSource -> IdMappingWorkflowInputSource.builder()
                          .inputSourceARN(inputSource.getInputSourceARN())
                          .schemaName(getNameFromArn(inputSource.getSchemaArn()))
                          .build())
                  .collect(Collectors.toList());
      }

      public static List<software.amazon.entityresolution.idmappingworkflow.IdMappingWorkflowInputSource> translateToCfnInputSourceConfig(
          List<IdMappingWorkflowInputSource> source, String workflowArn) {

          if (source == null) {
            return null;
          }

          return source.stream()
                  .map(inputSource -> software.amazon.entityresolution.idmappingworkflow.IdMappingWorkflowInputSource.builder()
                          .inputSourceARN(
                                  inputSource.inputSourceARN())
                          .schemaArn(
                                  buildSchemaArnFromWorkflowArn(
                                          inputSource.schemaName(),
                                          workflowArn))
                          .build())
                  .collect(Collectors.toList());
      }

      public static List<IdMappingWorkflowOutputSource> translateToVeniceOutputSourceConfig(
          List<software.amazon.entityresolution.idmappingworkflow.IdMappingWorkflowOutputSource> source) {

          if (source == null) {
            return null;
          }

          return source.stream()
                  .map(outputSource -> IdMappingWorkflowOutputSource.builder()
                          .outputS3Path(outputSource.getOutputS3Path())
                          .kmsArn(outputSource.getKMSArn())
                          .build())
                  .collect(Collectors.toList());
      }

      public static List<software.amazon.entityresolution.idmappingworkflow.IdMappingWorkflowOutputSource> translateToCfnOutputSourceConfig(
          List<IdMappingWorkflowOutputSource> source) {

          if (source == null) {
            return null;
          }

          return source.stream()
                  .map(outputSource -> software.amazon.entityresolution.idmappingworkflow.IdMappingWorkflowOutputSource.builder()
                          .kMSArn(
                                  outputSource.kmsArn())
                          .outputS3Path(
                                  outputSource.outputS3Path())
                          .build())
                  .collect(Collectors.toList());
      }

      public static IdMappingTechniques translateToVeniceResolutionTechniques(
          software.amazon.entityresolution.idmappingworkflow.IdMappingTechniques source) {

          if (source == null) {
            return null;
          }

          // Need to change the below logic once the 1p to 1p idmapping comes into place.
          if (!source.getIdMappingType()
                  .equals("PROVIDER")) {
            return null;
          }

          ProviderProperties.Builder providerPropertiesBuilder = ProviderProperties.builder()
                  .providerServiceArn(source.getProviderProperties().getProviderServiceArn());

          if (source.getProviderProperties().getProviderConfiguration() != null) {
              Map<String, Document> providerConfiguration = source.getProviderProperties().getProviderConfiguration()
                      .entrySet()
                      .stream()
                      .collect(Collectors.toMap(Map.Entry::getKey, entry -> Document.fromString(entry.getValue())));

              providerPropertiesBuilder.providerConfiguration(Document.fromMap(providerConfiguration));
          }

          if(source.getProviderProperties().getIntermediateSourceConfiguration() != null) {
              IntermediateSourceConfiguration intermediateSourceConfiguration = IntermediateSourceConfiguration.builder()
                      .intermediateS3Path(source.getProviderProperties().getIntermediateSourceConfiguration().getIntermediateS3Path())
                      .build();
              providerPropertiesBuilder.intermediateSourceConfiguration(intermediateSourceConfiguration);
          }

          return IdMappingTechniques.builder()
                  .idMappingType(source.getIdMappingType())
                  .providerProperties(providerPropertiesBuilder.build())
                  .build();
      }


      public static software.amazon.entityresolution.idmappingworkflow.IdMappingTechniques translateToCfnResolutionTechniques(
          IdMappingTechniques source) {
          if (source == null) {
            return null;
          }

          software.amazon.entityresolution.idmappingworkflow.ProviderProperties.ProviderPropertiesBuilder providerPropertiesBuilder =
                  software.amazon.entityresolution.idmappingworkflow.ProviderProperties.builder()
                          .providerServiceArn(source.providerProperties().providerServiceArn());

          if (source.providerProperties().providerConfiguration() != null) {
              Map<String, String> providerConfiguration = new HashMap<>();
              for (Map.Entry<String, Document> entry: source.providerProperties().providerConfiguration().asMap().entrySet()) {
                  providerConfiguration.put(entry.getKey(), entry.getValue().asString());
              }

              providerPropertiesBuilder.providerConfiguration(providerConfiguration);
          }

          if(source.providerProperties().intermediateSourceConfiguration() != null) {
              software.amazon.entityresolution.idmappingworkflow.IntermediateSourceConfiguration intermediateSourceConfiguration =
                      software.amazon.entityresolution.idmappingworkflow.IntermediateSourceConfiguration.builder()
                                                                                                        .intermediateS3Path(source.providerProperties().intermediateSourceConfiguration().intermediateS3Path())
                                                                                                        .build();
              providerPropertiesBuilder.intermediateSourceConfiguration(intermediateSourceConfiguration);
          }

          return software.amazon.entityresolution.idmappingworkflow.IdMappingTechniques.builder()
                  .idMappingType(source.idMappingTypeAsString())
                  .providerProperties(providerPropertiesBuilder.build())
                  .build();

      }

      public static Set<Tag> mapTagsToSet(Map<String, String> tags) {
          if (tags == null || tags.isEmpty()) {
            return null;
          }

          return tags.entrySet()
                  .stream()
                  .map(t -> Tag.builder()
                          .key(t.getKey())
                          .value(t.getValue())
                          .build())
                  .collect(Collectors.toSet());
      }

      public static String toWorkflowArn(final ResourceHandlerRequest<ResourceModel> request) {
          return String.format(WORKFLOW_ARN_FORMAT, request.getAwsPartition(), request.getRegion(),
                  request.getAwsAccountId(),
                  request.getDesiredResourceState()
                          .getWorkflowName());
      }

      public static String getNameFromArn(final String arn) {
          return arn.substring(arn.lastIndexOf("/") + 1);
      }

      public static String buildSchemaArnFromWorkflowArn(final String schemaName, final String workflowArn) {
          String prefix = workflowArn.substring(0, workflowArn.lastIndexOf(":") + 1);
          return prefix + "schemamapping/" + schemaName;
      }
}
