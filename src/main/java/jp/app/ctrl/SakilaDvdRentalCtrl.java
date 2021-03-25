package jp.app.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SakilaDvdRentalCtrl {
    public static final String[][] ODATA_ENTRY_INFOS = new String[][] { //
            { "SklActors",
                    "/SklActors?$filter=first_name%20eq%20'Adam'&$select=last_name&$orderby=last_name&$count=true",
                    "TABLE: actors" },
            { "SklActorInfos", "/SklActorInfos?$orderby=last_name,first_name,actor_id&$count=true&$top=20&$skip=3",
                    "VIEW: actor_info" },
            { "SklAddresss", "/SklAddresss?$count=true&$top=20", "" },
            { "SklCategorys", "/SklCategorys?$count=true&$top=20", "" },
            { "SklCitys", "/SklCitys?$count=true&$top=20&$orderby=country_id", "" },
            { "SklCountrys", "/SklCountrys?$count=true&$top=20&$orderby=country", "" },
            { "SklCustomers", "/SklCustomers?$count=true&$top=20", "" },
            { "SklCustomerLists", "/SklCustomerLists?$count=true&$top=20", "VIEW: " },
            { "SklFilms", "/SklFilms?$count=true&$top=20&$orderby=title", "" },
            { "SklFilmActors", "/SklFilmActors?$count=true&$top=20", "" },
            { "SklFilmCategorys", "/SklFilmCategorys?$count=true&$top=20", "" },
            { "SklFilmLists", "/SklFilmLists?$count=true&$top=20", "VIEW: " },
            { "SklInventorys", "/SklInventorys?$count=true&$top=20", "" },
            { "SklLanguages", "/SklLanguages?$count=true&$top=20", "" },
            { "SklNicerButSlowerFilmLists", "/SklNicerButSlowerFilmLists?$count=true&$top=20", "VIEW: " },
            { "SklPayments", "/SklPayments?$count=true&$top=20", "" },
            { "SklRentals", "/SklRentals?$count=true&$top=20", "" },
            { "SklSalesByFilmCategorys", "/SklSalesByFilmCategorys?$count=true&$top=20", "VIEW: " },
            { "SklSalesByStores", "/SklSalesByStores?$count=true&$top=20", "VIEW: " },
            { "SklStaffs", "/SklStaffs?$count=true&$top=20", "" },
            { "SklStaffLists",
                    "/SklStaffLists?$count=true&$top=20&$select=zip_code&$orderby=zip_code&$filter=zip_code ne '00000'",
                    "VIEW: " },
            { "SklStores", "/SklStores?$count=true", "" }, };

    @RequestMapping("/sakila-dvdrental.html")
    public String oiyokanUnittest(Model model) throws IOException {

        final List<UrlEntry> urlEntryList = new ArrayList<>();
        model.addAttribute("UrlEntryList", urlEntryList);

        for (String[] look : ODATA_ENTRY_INFOS) {
            urlEntryList.add(new UrlEntry(look[0], look[1], look[2]));
        }

        return "sakila-dvdrental";
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
