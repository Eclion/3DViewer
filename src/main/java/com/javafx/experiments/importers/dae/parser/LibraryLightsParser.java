package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion
 */
final class LibraryLightsParser extends DefaultHandler {
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();

    private enum State {
        UNKNOWN,
        adapt_thresh,
        area_shape,
        area_size,
        area_sizey,
        area_sizez,
        atm_distance_factor,
        atm_extinction_factor,
        atm_turbidity,
		att1,
		att2,
		backscattered_light,
		bias,
		blue,
		buffers,
		bufflag,
		bufsize,
		buftype,
		clipend,
		clipsta,
        color,
		compressthresh,
        constant_attenuation,
		dist,
		energy,
        extra,
		falloff_type,
		filtertype,
		flag,
		gamma,
		green,
		halo_intensity,
		horizon_brightness,
        light,
        linear_attenuation,
		mode,
        point,
        quadratic_attenuation,
		ray_samp,
		ray_samp_method,
		ray_samp_type,
		ray_sampy,
		ray_sampz,
		red,
		samp,
		shadhalostep,
		shadow_b,
		shadow_g,
		shadow_r,
		sky_colorspace,
		sky_exposure,
		skyblendfac,
		skyblendtype,
		soft,
		spotblend,
		spotsize,
		spread,
		sun_brightness,
		sun_effect_type,
		sun_intensity,
		sun_size,
        technique,
        technique_common,
		type
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
                System.out.println("Unknown element: " + qName);
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (state(qName)) {
            case UNKNOWN:
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }
}