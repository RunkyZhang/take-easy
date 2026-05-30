package com.rz.langchain.demo.server.tools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolsSelector {
    private final Map<String, ToolSpecification> toolSpecifications = new HashMap<>();
    private final Map<String, Object> tools = new HashMap<>();

    @Resource
    private WcImTools wcImTools;
    @Resource
    private WriteArticleAgent writeArticleAgent;
    @Resource
    private FormatAddressAgent formatAddressAgent;

    @PostConstruct
    private void init() {
        putTools(ToolSpecifications.toolSpecificationsFrom(wcImTools), wcImTools);
        putTools(ToolSpecifications.toolSpecificationsFrom(writeArticleAgent), writeArticleAgent);
        putTools(ToolSpecifications.toolSpecificationsFrom(formatAddressAgent), formatAddressAgent);
    }

    public Object getTool(String name) {
        return tools.get(name);
    }

    public List<ToolSpecification> getToolSpecifications() {
        return new ArrayList<>(toolSpecifications.values());
    }

    private void putTools(List<ToolSpecification> toolSpecifications, Object tools) {
        if (CollectionUtils.isEmpty(toolSpecifications)) {
            return;
        }

        for (ToolSpecification toolSpecification : toolSpecifications) {
            this.toolSpecifications.put(toolSpecification.name(), toolSpecification);
            this.tools.put(toolSpecification.name(), tools);
        }
    }
}
