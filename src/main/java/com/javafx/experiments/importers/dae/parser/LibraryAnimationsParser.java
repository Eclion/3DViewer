package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.importers.dae.parser.libraries.DaeAnimation;
import com.javafx.experiments.importers.dae.parser.libraries.DaeController;
import javafx.animation.Animation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Eclion
 */
final class LibraryAnimationsParser extends DefaultHandler {
    private final static Logger LOGGER = Logger.getLogger(LibraryAnimationsParser.class.getSimpleName());
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    private String currentAnimationId = "";
    public Map<String, DaeAnimation> animations = new HashMap<>();
    private LinkedList<DaeAnimation> currentAnimations = new LinkedList<>();

    private enum State {
        UNKNOWN,
        accessor,
        animation,
        channel,
        float_array,
        input,
        Name_array,
        param,
        sampler,
        source,
        technique_common
    }

    private static State state(String name) {
        try {
            return State.valueOf(name);
        } catch (Exception e) {
            return State.UNKNOWN;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentId.put(qName, attributes.getValue("id"));
        charBuf = new StringBuilder();
        switch (state(qName)) {
            case UNKNOWN:
                LOGGER.log(Level.WARNING, "Unknown element: " + qName);
                break;
            case animation:
                currentAnimationId = currentId.get(qName);
                currentAnimations.push(new DaeAnimation(currentAnimationId));
                //animations.put(currentAnimationId, );
                break;
            case channel:
                currentAnimations.peek().target = attributes.getValue("target");
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (state(qName)) {
            case animation:
                DaeAnimation animation = currentAnimations.pop();
                if(currentAnimations.isEmpty())
                {
                    animations.put(currentAnimationId, animation);
                }
                else
                {
                    currentAnimations.peek().addChild(animation);
                }
                break;
            case float_array:
                String sourceId = currentId.get(State.source.name());
                if (sourceId.equalsIgnoreCase(currentAnimationId+"-input"))
                {
                    currentAnimations.peek().input = ParserUtils.extractFloatArray(charBuf);
                }
                else if (sourceId.equalsIgnoreCase(currentAnimationId+"-output"))
                {
                    currentAnimations.peek().output = ParserUtils.extractDoubleArray(charBuf);
                }
                break;
            case Name_array:
                currentAnimations.peek().setInterpolations(
                        ParserUtils.extractNameArray(charBuf)
                );
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }

    public void build(DaeController controller)
    {
        // cf org.jagatoo.loaders.models.collada.LibraryAnimationsLoader
    }
}
