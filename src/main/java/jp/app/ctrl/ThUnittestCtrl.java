package jp.app.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ThUnittestCtrl {
    public static final String[][] ODATA_ENTRY_INFOS = new String[][] { //
            { "root", "/", "Root of OData v4 server." },
            { "version", "/ODataAppInfos?$format=JSON", "Show Oiyokan version." },
            { "$metadata", "/$metadata", "Metadata of OData." },
            { "ODataTests1 : $orderby", "/ODataTests1?$orderby=ID&$top=20&$count=true", "Sort test." },
            { "ODataTests1 : $filter",
                    "/ODataTests1?$top=2001&$filter=Description eq 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)' and ID eq 1.0&$count=true&$select=ID,Name",
                    "Filter test." },
            { "ODataTests1 : $search (Experimental)", "/ODataTestFulls1?$top=6&$search=macbook&$count=true&$select=ID",
                    "Full text search test (Experimental)." }, };

    @RequestMapping("/unittest.html")
    public String oiyokanUnittest(Model model) throws IOException {

        final List<UrlEntryBean> urlEntryList = new ArrayList<>();
        model.addAttribute("UrlEntryList", urlEntryList);

        for (String[] look : ODATA_ENTRY_INFOS) {
            urlEntryList.add(new UrlEntryBean(look[0], look[1], look[2]));
        }

        return "unittest";
    }
}
