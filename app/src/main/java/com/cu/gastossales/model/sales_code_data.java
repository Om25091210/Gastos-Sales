package com.cu.gastossales.model;

import java.io.Serializable;

public class sales_code_data implements Serializable {
    private String sales_code;
    private String sales_name;
    private String total_providers;

    public sales_code_data() {
    }

    public sales_code_data(String sales_code, String sales_name, String total_providers) {
        this.sales_code = sales_code;
        this.sales_name = sales_name;
        this.total_providers = total_providers;
    }

    public String getSales_name() {
        return sales_name;
    }

    public void setSales_name(String sales_name) {
        this.sales_name = sales_name;
    }

    public String getSales_code() {
        return sales_code;
    }

    public void setSales_code(String sales_code) {
        this.sales_code = sales_code;
    }

    public String getTotal_providers() {
        return total_providers;
    }

    public void setTotal_providers(String total_providers) {
        this.total_providers = total_providers;
    }
}
