package software.amazon.entityresolution.matchingworkflow;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.entityresolution.model.InputSource;
import software.amazon.awssdk.services.entityresolution.model.OutputAttribute;
import software.amazon.awssdk.services.entityresolution.model.OutputSource;
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
