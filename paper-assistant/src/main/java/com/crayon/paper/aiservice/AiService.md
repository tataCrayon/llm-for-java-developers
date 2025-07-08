LangChain4j的服务@AiService和使用Spring服务不一样，其并不适合作为Bean使用。  
比如需要动态设置ChatMemory，但是Spring的Bean是单例的，所以无法实现。

所以正确的做法是，通过服务层管理 AiService 实例。