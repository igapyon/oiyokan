package jp.app.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SakilaDvdRentalCtrl {
    @RequestMapping("/sakila-dvdrental.html")
    public String oiyokanUnittest(Model model) throws IOException {

        final List<UrlEntry> urlEntryList = new ArrayList<>();
        model.addAttribute("UrlEntryList", urlEntryList);

        urlEntryList.add(new UrlEntry("SklActors", "/SklActors?$filter=first_name%20eq%20'Adam'&$select=last_name&$orderby=last_name&$count=true&$top=20", "TABLE: actors"));
        urlEntryList.add(new UrlEntry("SklActorInfos", "/SklActorInfos?$count=true&$top=20", "VIEW: actor_info"));
        urlEntryList.add(new UrlEntry("SklAddresss", "/SklAddresss?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklCategorys", "/SklCategorys?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklCitys", "/SklCitys?$count=true&$top=20&$orderby=country_id", ""));
        urlEntryList.add(new UrlEntry("SklCountrys", "/SklCountrys?$count=true&$top=20&$orderby=country", ""));
        urlEntryList.add(new UrlEntry("SklCustomers", "/SklCustomers?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklCustomerLists", "/SklCustomerLists?$count=true&$top=20", "VIEW: "));
        urlEntryList.add(new UrlEntry("SklFilms", "/SklFilms?$count=true&$top=20&$orderby=title", ""));
        urlEntryList.add(new UrlEntry("SklFilmActors", "/SklFilmActors?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklFilmCategorys", "/SklFilmCategorys?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklFilmLists", "/SklFilmLists?$count=true&$top=20", "VIEW: "));
        urlEntryList.add(new UrlEntry("SklInventorys", "/SklInventorys?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklLanguages", "/SklLanguages?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklNicerButSlowerFilmLists", "/SklNicerButSlowerFilmLists?$count=true&$top=20",
                "VIEW: "));
        urlEntryList.add(new UrlEntry("SklPayments", "/SklPayments?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklRentals", "/SklRentals?$count=true&$top=20", ""));
        urlEntryList
                .add(new UrlEntry("SklSalesByFilmCategorys", "/SklSalesByFilmCategorys?$count=true&$top=20", "VIEW: "));
        urlEntryList.add(new UrlEntry("SklSalesByStores", "/SklSalesByStores?$count=true&$top=20", "VIEW: "));
        urlEntryList.add(new UrlEntry("SklStaffs", "/SklStaffs?$count=true&$top=20", ""));
        urlEntryList.add(new UrlEntry("SklStaffLists",
                "/SklStaffLists?$count=true&$top=20&$select=zip_code&$orderby=zip_code&$filter=zip_code ne '00000'",
                "VIEW: "));
        urlEntryList.add(new UrlEntry("SklStores", "/SklStores?$count=true", ""));

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
