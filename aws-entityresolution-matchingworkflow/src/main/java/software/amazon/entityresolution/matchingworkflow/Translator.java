package software.amazon.entityresolution.matchingworkflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.entityresolution.model.InputSource;
import software.amazon.awssdk.services.entityresolution.model.IntermediateSourceConfiguration;
import software.amazon.awssdk.services.entityresolution.model.OutputAttribute;
import software.amazon.awssdk.services.entityresolution.model.OutputSource;
import software.amazon.awssdk.services.entityresolution.model.ProviderProperties;
import software.amazon.awssdk.services.entityresolution.model.ResolutionTechniques;
import software.amazon.awssdk.services.entityresolution.model.Rule;
import software.amazon.awssdk.services.entityresolution.model.RuleBasedProperties;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * This class is a centralized placeholder for - api request construction - object translation to/from aws sdk -
 * resource model construction for read/list handlers
 */
public class Translator {

    public static String WORKFLOW_ARN_FORMAT = "arn:%s:entityresolution:%s:%s:matchingworkflow/%s";

    public static List<InputSource> translateToVeniceInputSourceConfig(
        List<software.amazon.entityresolution.matchingworkflow.InputSource> source) {
        if (source == null) {
            return null;
        }

        return source.stream()
                     .map(inputSource -> InputSource.builder()
                                                    .inputSourceARN(inputSource.getInputSourceARN())
                                                    .schemaName(getNameFromArn(inputSource.getSchemaArn()))
                                                    .applyNormalization(inputSource.getApplyNormalization())
                                                    .build())
                     .collect(Collectors.toList());
    }

    public static List<software.amazon.entityresolution.matchingworkflow.InputSource> translateToCfnInputSourceConfig(
        List<InputSource> source, String workflowArn) {
        if (source == null) {
            return null;
        }

        return source.stream()
                     .map(inputSource -> software.amazon.entityresolution.matchingworkflow.InputSource.builder()
                                                                                                      .inputSourceARN(
                                                                                                          inputSource.inputSourceARN())
                                                                                                      .schemaArn(
                                                                                                          buildSchemaArnFromWorkflowArn(
                                                                                                              inputSource.schemaName(),
                                                                                                              workflowArn))
                                                                                                      .applyNormalization(
                                                                                                          inputSource.applyNormalization())
                                                                                                      .build())
                     .collect(Collectors.toList());
    }

    public static List<OutputSource> translateToVeniceOutputSourceConfig(
        List<software.amazon.entityresolution.matchingworkflow.OutputSource> source) {
        if (source == null) {
            return null;
        }

        return source.stream()
                     .map(outputSource -> OutputSource.builder()
                                                      .output(outputSource.getOutput() == null ? null
                                                          : outputSource.getOutput()
                                                                        .stream()
                                                                        .map(
                                                                            outputAttribute -> OutputAttribute.builder()
                                                                                                              .name(
                                                                                                                  outputAttribute.getName())
                                                                                                              .hashed(
                                                                                                                  outputAttribute.getHashed())
                                                                                                              .build())
                                                                        .collect(Collectors.toList()))
                                                      .outputS3Path(outputSource.getOutputS3Path())
                                                      .applyNormalization(outputSource.getApplyNormalization())
                                                      .kmsArn(outputSource.getKMSArn())
                                                      .build())
                     .collect(Collectors.toList());
    }

    public static List<software.amazon.entityresolution.matchingworkflow.OutputSource> translateToCfnOutputSourceConfig(
        List<OutputSource> source) {
        if (source == null) {
            return null;
        }

        return source.stream()
                     .map(outputSource -> software.amazon.entityresolution.matchingworkflow.OutputSource.builder()
                                                                                                        .applyNormalization(
                                                                                                            outputSource.applyNormalization())
                                                                                                        .kMSArn(
                                                                                                            outputSource.kmsArn())
                                                                                                        .outputS3Path(
                                                                                                            outputSource.outputS3Path())
                                                                                                        .output(
                                                                                                            outputSource.output()
                                                                                                                        .stream()
                                                                                                                        .map(
                                                                                                                            outputAttribute -> software.amazon.entityresolution.matchingworkflow.OutputAttribute.builder()
                                                                                                                                                                                                                .name(
                                                                                                                                                                                                                    outputAttribute.name())
                                                                                                                                                                                                                .hashed(
                                                                                                                                                                                                                    outputAttribute.hashed())
                                                                                                                                                                                                                .build())
                                                                                                                        .collect(
                                                                                                                            Collectors.toList()))
                                                                                                        .build())
                     .collect(Collectors.toList());
    }

    public static ResolutionTechniques translateToVeniceResolutionTechniques(
        software.amazon.entityresolution.matchingworkflow.ResolutionTechniques source) {
        if (source == null) {
            return null;
        }

        if (source.getResolutionType()
                  .equals("ML_MATCHING")) {
            return ResolutionTechniques.builder()
                                       .resolutionType(source.getResolutionType())
                                       .build();
        } else if(source.getResolutionType().equals("PROVIDER")) {

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

            return ResolutionTechniques.builder()
                                       .resolutionType(source.getResolutionType())
                                       .providerProperties(providerPropertiesBuilder.build())
                                       .build();
        }

        return ResolutionTechniques.builder()
                                   .resolutionType(source.getResolutionType())
                                   .ruleBasedProperties(RuleBasedProperties.builder()
                                                                           .attributeMatchingModel(
                                                                               source.getRuleBasedProperties()
                                                                                     .getAttributeMatchingModel())
                                                                           .rules(source.getRuleBasedProperties()
                                                                                        .getRules()
                                                                                        .stream()
                                                                                        .map(rule -> Rule.builder()
                                                                                                         .ruleName(
                                                                                                             rule.getRuleName())
                                                                                                         .matchingKeys(
                                                                                                             rule.getMatchingKeys())
                                                                                                         .build())
                                                                                        .collect(
                                                                                            Collectors.toList()))
                                                                           .build())
                                   .build();
    }

    public static software.amazon.entityresolution.matchingworkflow.ResolutionTechniques translateToCfnResolutionTechniques(
        ResolutionTechniques source) {
        if (source == null) {
            return null;
        }

        if (source.resolutionTypeAsString()
                  .equals("ML_MATCHING")) {
            return software.amazon.entityresolution.matchingworkflow.ResolutionTechniques.builder()
                                                                                         .resolutionType(
                                                                                             source.resolutionTypeAsString())
                                                                                         .build();
        } else if (source.resolutionTypeAsString().equals("PROVIDER")) {

            software.amazon.entityresolution.matchingworkflow.ProviderProperties.ProviderPropertiesBuilder providerPropertiesBuilder =
                software.amazon.entityresolution.matchingworkflow.ProviderProperties.builder()
                                                                                    .providerServiceArn(source.providerProperties().providerServiceArn());

            if (source.providerProperties().providerConfiguration() != null) {
                Map<String, String> providerConfiguration = new HashMap<>();
                for (Map.Entry<String, Document> entry: source.providerProperties().providerConfiguration().asMap().entrySet()) {
                    providerConfiguration.put(entry.getKey(), entry.getValue().asString());
                }

                providerPropertiesBuilder.providerConfiguration(providerConfiguration);
            }

            if(source.providerProperties().intermediateSourceConfiguration() != null) {
                software.amazon.entityresolution.matchingworkflow.IntermediateSourceConfiguration intermediateSourceConfiguration = software.amazon.entityresolution.matchingworkflow.IntermediateSourceConfiguration.builder()
                                                                                                                                                                                                                     .intermediateS3Path(source.providerProperties().intermediateSourceConfiguration().intermediateS3Path())
                                                                                                                                                                                                                     .build();
                providerPropertiesBuilder.intermediateSourceConfiguration(intermediateSourceConfiguration);
            }

            return software.amazon.entityresolution.matchingworkflow.ResolutionTechniques.builder()
                                                                                         .resolutionType(source.resolutionTypeAsString())
                                                                                         .providerProperties(providerPropertiesBuilder.build())
                                                                                         .build();
        }

        return software.amazon.entityresolution.matchingworkflow.ResolutionTechniques.builder()
                                                                                     .resolutionType(
                                                                                         source.resolutionTypeAsString())
                                                                                     .ruleBasedProperties(
                                                                                         software.amazon.entityresolution.matchingworkflow.RuleBasedProperties.builder()
                                                                                                                                                              .attributeMatchingModel(
                                                                                                                                                                  source.ruleBasedProperties()
                                                                                                                                                                        .attributeMatchingModelAsString())
                                                                                                                                                              .rules(
                                                                                                                                                                  source.ruleBasedProperties()
                                                                                                                                                                        .rules()
                                                                                                                                                                        .stream()
                                                                                                                                                                        .map(
                                                                                                                                                                            rule ->
                                                                                                                                                                                software.amazon.entityresolution.matchingworkflow.Rule.builder()
                                                                                                                                                                                                                                      .ruleName(
                                                                                                                                                                                                                                          rule.ruleName())
                                                                                                                                                                                                                                      .matchingKeys(
                                                                                                                                                                                                                                          rule.matchingKeys())
                                                                                                                                                                                                                                      .build())
                                                                                                                                                                        .collect(
                                                                                                                                                                            Collectors.toList()))
                                                                                                                                                              .build())
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
