package com.rquest.riskmaster.entity130;

import java.util.Date;

public class BondIndexs {
    private String idInstrument;

    private String cdSymbol;

    private String nameIndex;

    private String cdExchange;

    private Date timestamp;

    public String getIdInstrument() {
        return idInstrument;
    }

    public void setIdInstrument(String idInstrument) {
        this.idInstrument = idInstrument == null ? null : idInstrument.trim();
    }

    public String getCdSymbol() {
        return cdSymbol;
    }

    public void setCdSymbol(String cdSymbol) {
        this.cdSymbol = cdSymbol == null ? null : cdSymbol.trim();
    }

    public String getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(String nameIndex) {
        this.nameIndex = nameIndex == null ? null : nameIndex.trim();
    }

    public String getCdExchange() {
        return cdExchange;
    }

    public void setCdExchange(String cdExchange) {
        this.cdExchange = cdExchange == null ? null : cdExchange.trim();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}