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
            { "SklAddresses", "/SklAddresses?$filter=district%20eq%20%27Kanagawa%27&$orderby=city_id&$count=true&$top=20", "TABLE: address" },
            { "SklCategories", "/SklCategories?$filter=name%20ge%20%27Music%27%20and%20name%20ge%20%27Sports%27&$orderby=name&$count=true&$top=20", "TABLE: category" },
            { "SklCities", "/SklCities?$filter=city_id%20eq%20256&$count=true&$top=20&$orderby=country_id", "TABLE: city" },
            { "SklCountries", "/SklCountries?$filter=country%20ge%20%27Japan%27%20and%20country_id%20lt%2054&$count=true&$top=20&$orderby=country", "TABLE: country" },
            { "SklCustomers", "/SklCustomers?$filter=create_date%20le%202006-02-14%20and%20contains(first_name,%27Do%27)%20and%20activebool%20eq%20true&$orderby=store_id,last_name&$count=true&$top=20", "TABLE: customer" },
            { "SklCustomerLists", "/SklCustomerLists?$filter=country%20eq%20%27Taiwan%27%20and%20zip_code%20ge%20%2795000%27&$orderby=sid&$count=true&$top=20", "VIEW: customer_list" },
            { "SklFilms", "/SklFilms?$filter=replacement_cost%20ge%2029%20and%20rental_rate%20lt%201&$orderby=length,title&$count=true&$top=20", "TABLE: film" },
            { "SklFilmActors", "/SklFilmActors?$filter=actor_id%20ge%2030%20and%20actor_id%20lt%2031%20and%20film_id%20gt%20700&$orderby=film_id%20desc&$count=true&$top=20", "TABLE: film_actor" },
            { "SklFilmCategories", "/SklFilmCategories?$count=true&$top=20", "TABLE: film_category" },
            { "SklFilmLists", "/SklFilmLists?$count=true&$top=20", "VIEW: film_list" },
            { "SklInventories", "/SklInventories?$count=true&$top=20", "TABLE: inventory" },
            { "SklLanguages", "/SklLanguages?$count=true&$top=20", "TABLE: language" },
            { "SklNicerButSlowerFilmLists", "/SklNicerButSlowerFilmLists?$count=true&$top=20",
                    "VIEW: nicer_but_slower_film_list" },
            { "SklPayments", "/SklPayments?$count=true&$top=20", "TABLE: payment" },
            { "SklRentals", "/SklRentals?$count=true&$top=20", "TABLE: rental" },
            { "SklSalesByFilmCategories", "/SklSalesByFilmCategories?$count=true&$top=20",
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
