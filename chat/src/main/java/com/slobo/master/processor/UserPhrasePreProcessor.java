package com.slobo.master.processor;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.types.InstanceList;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class UserPhrasePreProcessor {

    public List<String> process(String phrase) {

        String stopListFilePath = "S:\\git_rep\\master\\chat\\src\\main\\resources\\data\\en.txt";

        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new Input2CharSequence("UTF-8"));
        Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));
        pipeList.add(new TokenSequenceLowercase());
        pipeList.add(new TokenSequenceRemoveStopwords(new File(stopListFilePath), "utf-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());
        pipeList.add(new Target2Label());
        pipeList.add(new PrintInputAndTarget());
        SerialPipes pipeline = new SerialPipes(pipeList);


        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipeline);

        String[] data = new String[]{"Gallery unveils interactive tree A Christmas tree that can receive text messages has been unveiled at London's Tate Britain art gallery."};
        // Now process each instance provided by the iterator.
        instances.addThruPipe(new ArrayIterator(data));

        return Arrays.asList(data);

    }
}
