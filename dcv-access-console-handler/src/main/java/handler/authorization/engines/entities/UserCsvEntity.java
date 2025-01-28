// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization.engines.entities;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.customconverter.ConvertGermanToBoolean;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserCsvEntity {
    @CsvBindByName
    private String userId;

    @CsvBindByName
    private String displayName;

    @CsvBindByName
    private String role;

    @CsvBindAndSplitByName(elementType = String.class, collectionType = ArrayList.class, splitOn = "\\|")
    private List<String> groups;
}
