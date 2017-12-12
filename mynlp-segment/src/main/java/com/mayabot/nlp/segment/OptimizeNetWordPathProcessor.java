package com.mayabot.nlp.segment;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.WordPath;
import com.mayabot.nlp.segment.wordnet.Wordnet;

import java.util.List;
import java.util.Map;

/**
 * 优化网络处理器
 */
public class OptimizeNetWordPathProcessor implements WordPathProcessor,WordPathProcessorIniter {

    private final NamedComponentRegistry registry;

    private List<OptimizeProcessor> optimizeProcessorList = Lists.newArrayList();

    @Inject
    public OptimizeNetWordPathProcessor(NamedComponentRegistry registry){
        this.registry = registry;
    }

    @Override
    public void init(Map<String, Object> map) {
        List<String> list = (List) map.get("list");

        Preconditions.checkArgument(!list.isEmpty(),"");

        for (String name : list) {
            OptimizeProcessor pr = registry.getInstance(name, OptimizeProcessor.class);
            Preconditions.checkNotNull(pr, "Not found OptimizeProcessor " + name);
            optimizeProcessorList.add(pr);
        }
    }

    @Override

    public WordPath process(WordPath wordPath) {

        Wordnet wordnet = wordPath.getWordnet();

        //标记处优化网络
        wordnet.setOptimizeNet(true);
        wordnet.tagOptimizeNetVertex(wordPath);

        Vertex[] pathWithBE = new Vertex[wordPath.wordCount() + 2];
        int i = 0;
        for (Vertex v : wordPath.getBestPathWithBE()) {
            pathWithBE[i++] = v;
        }

        boolean change = false;
        for (OptimizeProcessor processor : optimizeProcessorList) {
            boolean ch = processor.process(pathWithBE, wordnet);
            if (ch) {
                change = true;
            }
        }

        if (change) {
            wordPath = wordPath.getBestPathComputer().select(wordnet);
        }

        wordnet.setOptimizeNet(false);

        return wordPath;
    }

}

