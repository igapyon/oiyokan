package jp.app.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OiyokanUnittestCtrl {
    @RequestMapping("/oiyokan-unittest.html")
    public String oiyokanUnittest(Model model) throws IOException {

        final List<UrlEntry> urlEntryList = new ArrayList<>();
        model.addAttribute("UrlEntryList", urlEntryList);

        urlEntryList.add(new UrlEntry("root", "/", "Root of OData v4 server."));
        urlEntryList.add(new UrlEntry("version", "/ODataAppInfos?$format=JSON", "Show Oiyokan version."));
        urlEntryList.add(new UrlEntry("$metadata", "/$metadata", "Metadata of OData."));
        urlEntryList
                .add(new UrlEntry("MyProduct : $orderby", "/MyProducts?$orderby=ID&$top=20&$count=true", "Sort test."));
        urlEntryList.add(new UrlEntry("MyProduct : $filter",
                "/MyProducts?$top=2001&$filter=Description eq 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)' and ID eq 1.0&$count=true&$select=ID,Name",
                "Filter test."));
        urlEntryList.add(new UrlEntry("MyProduct : $search (Experimental)",
                "/MyProductFulls?$top=6&$search=macbook&$count=true&$select=ID",
                "Full text search test (Experimental)."));

        return "oiyokan-unittest";
    }

    public static class UrlEntry {
        private String name;
        private String path;
        private String note = "何か説明.";

        public UrlEntry(String name, String path, String note) {
            this.name = name;
            this.path = path;
            this.note = note;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        ///////////
        public String getPathWithTrimg() {
            if (path.length() > 36) {
                return path.substring(0, Math.min(path.length(), 36)) + "...";
            } else {
                return path;
            }
        }
    }
}
