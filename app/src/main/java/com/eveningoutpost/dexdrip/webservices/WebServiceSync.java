package com.eveningoutpost.dexdrip.webservices;

import com.eveningoutpost.dexdrip.Models.DesertSync;
import com.eveningoutpost.dexdrip.Models.UserError;

import java.util.List;

/**
 * Created by jamorham on 18/08/2018.
 *
 */

public class WebServiceSync extends BaseWebService {

    private static String TAG = "WebServiceSync";

    // process the request and produce a response object
    public WebResponse request(String query) {

        UserError.Log.d(TAG, query);

        if (!DesertSync.isEnabled()) {
            return webError("Desert Sync option not enabled in Sync Settings");
        }

        query = stripFirstComponent(query);

        List<String> components = getUrlComponents(query);

        if (components.size() > 0) {
            UserError.Log.d(TAG, "Processing " + query);
            switch (components.get(0)) {

                case "id":
                    return webOk(DesertSync.getMyRollCall());

                case "push":

                    if (components.size() == 4) {
                        final String topic = components.get(1);
                        final String sender = components.get(2);
                        final String payload = urlDecode(components.get(3));

                        if (DesertSync.fromPush(topic, sender, payload)) {
                            return webOk("OK");
                        } else {
                            return webError("Invalid parameters");
                        }

                    } else {
                        return webError("Incorrect parameter count for Push " + components.size(), 400);
                    }


                case "pull":
                    if (components.size() == 3) {
                        try {
                            final long since = Long.parseLong(components.get(1));
                            final String topic = components.get(2);
                            final String result = getFromPosition(since, topic);

                           /*    int counter = 0;
                               while (result == null && counter < 10) {
                                   JoH.threadSleep(3000);
                                   counter++;
                               }*/

                            if (result == null) {
                                return webOk(DesertSync.NO_DATA_MARKER);
                            } else {
                                return webOk(result);
                            }

                        } catch (NumberFormatException e) {
                            return webError("Couldn't parse Counter value: " + components.get(1));
                        }
                    } else {
                        return webError("Incorrect parameters for Pull", 400);
                    }
                    //break;
                default:
                    return webError("Unknown sync command: " + components.get(0));
            }

        } else {
            return webError("No Sync command specified", 404);
        }
    }

    private String getFromPosition(final long since, final String topic) {
        final List<DesertSync> list = DesertSync.since(since, topic);
        if (list != null) {
            if (list.size() > 0) {
                return DesertSync.toJson(list);
            }
        }
        return null;
    }
}