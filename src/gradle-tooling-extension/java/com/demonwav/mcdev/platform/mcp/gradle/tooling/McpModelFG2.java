package com.demonwav.mcdev.platform.mcp.gradle.tooling;

import java.util.Set;

public interface McpModelFG2 extends McpModel {
    String getMinecraftVersion();
    Set<String> getMappingFiles();
}