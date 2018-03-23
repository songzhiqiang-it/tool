package com.rquest.riskmaster.mapper130;

import com.rquest.riskmaster.entity130.BondIndexs;

public interface BondIndexsMapper {
    int deleteByPrimaryKey(String idInstrument);

    int insert(BondIndexs record);

    int insertSelective(BondIndexs record);

    BondIndexs selectByPrimaryKey(String idInstrument);

    int updateByPrimaryKeySelective(BondIndexs record);

    int updateByPrimaryKey(BondIndexs record);
}