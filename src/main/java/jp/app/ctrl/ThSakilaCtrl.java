package jp.app.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ThSakilaCtrl {
    public static final String[][] ODATA_ENTRY_INFOS = new String[][] { //
            { "SklActors",
                    "/SklActors?$filter=first_name%20eq%20%27Adam%27&$select=last_name&$orderby=last_name&$count=true",
                    "TABLE: actors" },
            { "SklActorInfos", "/SklActorInfos?$orderby=last_name,first_name,actor_id&$count=true&$top=20&$skip=3",
                    "VIEW: actor_info" },
            { "SklAddresss", "/SklAddresss?$count=true&$top=20", "TABLE: address" },
            { "SklCategorys", "/SklCategorys?$count=true&$top=20", "TABLE: category" },
            { "SklCitys", "/SklCitys?$count=true&$top=20&$orderby=country_id", "TABLE: city" },
            { "SklCountrys", "/SklCountrys?$count=true&$top=20&$orderby=country", "TABLE: country" },
            { "SklCustomers", "/SklCustomers?$count=true&$top=20", "TABLE: customer" },
            { "SklCustomerLists", "/SklCustomerLists?$count=true&$top=20", "VIEW: customer_list" },
            { "SklFilms", "/SklFilms?$count=true&$top=20&$orderby=title", "TABLE: film" },
            { "SklFilmActors", "/SklFilmActors?$count=true&$top=20", "TABLE: film_actor" },
            { "SklFilmCategorys", "/SklFilmCategorys?$count=true&$top=20", "TABLE: film_category" },
            { "SklFilmLists", "/SklFilmLists?$count=true&$top=20", "VIEW: film_list" },
            { "SklInventorys", "/SklInventorys?$count=true&$top=20", "TABLE: inventory" },
            { "SklLanguages", "/SklLanguages?$count=true&$top=20", "TABLE: language" },
            { "SklNicerButSlowerFilmLists", "/SklNicerButSlowerFilmLists?$count=true&$top=20",
                    "VIEW: nicer_but_slower_film_list" },
            { "SklPayments", "/SklPayments?$count=true&$top=20", "TABLE: payment" },
            { "SklRentals", "/SklRentals?$count=true&$top=20", "TABLE: rental" },
            { "SklSalesByFilmCategorys", "/SklSalesByFilmCategorys?$count=true&$top=20",
                    "VIEW: sales_by_film_category" },
            { "SklSalesByStores", "/SklSalesByStores?$count=true&$top=20", "VIEW: sales_by_store" },
            { "SklStaffs", "/SklStaffs?$count=true&$top=20", "TABLE: staff" },
            { "SklStaffLists",
                    "/SklStaffLists?$count=true&$top=20&$select=zip_code&$orderby=zip_code&$filter=zip_code%20ne%20%2700000%27",
                    "VIEW: staff_list" },
            { "SklStores", "/SklStores?$count=true", "TABLE: store" }, };

    @RequestMapping("/sakila.html")
    public String oiyokanUnittest(Model model) throws IOException {

        final List<UrlEntryBean> urlEntryList = new ArrayList<>();
        model.addAttribute("UrlEntryList", urlEntryList);

        for (String[] look : ODATA_ENTRY_INFOS) {
            urlEntryList.add(new UrlEntryBean(look[0], look[1], look[2]));
        }

        return "sakila";
    }
}
