package com.sun.mediacontroller;

import com.blankj.utilcode.util.LogUtils;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

/**
 * @author Sun
 * @date 2019/1/29 14:26
 * @desc
 */
public class ClingUtil {
    private static final String DIDL_LITE_HEADER = "<DIDL-Lite " +
            "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
            "xmlns:sec=\"http://www.sec.co.kr/\">";
    private static final String DIDL_LITE_FOOTER = "</DIDL-Lite>";

    /**
     * 获取 Item 资源的 metadata
     */
    public static String getItemMetadata(String playUrl) {
        String id = "p1302";
        String title = "video";
        String c = "Anonymous";

        Res itemRes = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), 0L, playUrl);
        VideoItem item = new VideoItem(id, "0", title, c, itemRes);

        StringBuilder metadata = new StringBuilder();
        metadata.append(DIDL_LITE_HEADER);
        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.getId(), item.getParentID(), item.isRestricted() ? "1" : "0"));
        metadata.append(String.format("<dc:title>%s</dc:title>", item.getTitle()));

        String creator = item.getCreator();
        if (creator != null) {
            creator = creator.replaceAll("<", "_");
            creator = creator.replaceAll(">", "_");
        }
        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.getClazz().getValue()));
        metadata.append(String.format("<upnp:creator>%s</upnp:creator>", creator));

        Res res = item.getFirstResource();
        if (res != null) {
            // protocol info
            String protocolInfo = "";
            ProtocolInfo pi = res.getProtocolInfo();
            if (pi != null) {
                protocolInfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.getProtocol(), pi.getNetwork(), pi.getContentFormatMimeType(), pi.getAdditionalInfo());
            }
            LogUtils.i("protocolInfo: " + protocolInfo);

            // resolution, extra info, not adding yet
            String resolution = "";
            if (res.getResolution() != null && res.getResolution().length() > 0) {
                resolution = String.format("resolution=\"%s\"", res.getResolution());
            }

            // duration
            String duration = "";
            if (res.getDuration() != null && res.getDuration().length() > 0) {
                duration = String.format("duration=\"%s\"", res.getDuration());
            }

            // res begin
            // metadata.append(String.format("<res %s>", protocolInfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolInfo, resolution, duration));

            // url
            String url = res.getValue();
            metadata.append(url);

            // res end
            metadata.append("</res>");
        }
        metadata.append("</item>");
        metadata.append(DIDL_LITE_FOOTER);
        return metadata.toString();
    }
}
