# 其他

将SerpAPI作为tool使用后apiKey无效。所以这些tool废弃了。
可以使用Http工具，或者和LangChain4j示例一样，封装成WebSearchTool集成进ChatModel。

# 注意事项

为了增加 LLM 使用正确的参数调用正确的工具的机会，我们应该提供一个清晰且无歧义的：

- 工具的名称
- 工具的功能描述以及使用场景
- 每个工具参数的描述

一个不错的经验法则：如果人类能够理解工具的用途以及如何使用它，那么 LLM 很可能也能理解。

# @Tool注解说明

详情见官网 https://docs.langchain4j.dev/tutorials/tools
这里简单说明一下：

LangChain4j推荐使用@Tool注解来描述工具。

- 属性

name：工具名称
value: 工具描述

- 方法限制  
  被 @Tool 注释的方法可以是静态的或非静态的，可以有任意的可见性（公共、私有等）。

- 方法参数  
  基本支持

- 必选和可选  
  默认参数必填，可选代码如下

```java
record User(String name, @JsonProperty(required = false) String email) {}

    @Tool
    void add(User user) {
    ...
    } 
```  

# 工具列表与设计模式

因为工具最终的归属是工具列表，所以并无设计模式的需求。
