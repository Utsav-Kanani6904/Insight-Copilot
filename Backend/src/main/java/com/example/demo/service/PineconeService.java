package com.example.demo.service;

import com.example.demo.entity.TableInfo;
import com.example.demo.repository.TableInfoRepository;
import io.pinecone.clients.Index;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.SearchRecordsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PineconeService {

    @Autowired
    private Index index;

    @Autowired
    private TableInfoRepository tableInfoRepository;

    public SearchRecordsResponse makeQuery() throws ApiException {
        try {

            String query = "How many students are there who have not submitted any assignment in their related courses";
//            List<String> fields = new ArrayList<>();
//            fields.add("category");
//            fields.add("chunk_text");

            // Search the dense index
            SearchRecordsResponse recordsResponse = index.searchRecordsByText(query, "table-details-university", null, 4, null, null);

            // Print the results
            System.out.println(recordsResponse);
            return recordsResponse;
        } catch (Exception e) {
            System.out.println("Something went wrong while retrieving the answer for query");
            System.out.println(e);
            throw e;
        }
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(", ", list);
    }

    public void upsertTableRecord(List<TableInfo>tableInfoList) throws Exception {
        try {
            List<TableInfo> savedTableInfo = tableInfoRepository.saveAll(tableInfoList);

            ArrayList<Map<String, String>> upsertRecords = new ArrayList<>();

            for (TableInfo table : savedTableInfo) {
                Map<String, String> record = new HashMap<>();
                record.put("_id", table.getId().toString());
                record.put("name", table.getTableName());
                record.put("chunk_text", table.getTableName() + " : " + table.getDescription());
                record.put("joinsWith", listToString(table.getJoinsWith()));
                record.put("primaryKeys", listToString(table.getPrimaryKeys()));
                record.put("foreignKeys", listToString(table.getForeignKeys()));

                upsertRecords.add(record);
            }

            index.upsertRecords("testing-namespace", upsertRecords);

        } catch (Exception e) {
            System.out.println("Something went wrong while uploading table information......");
            System.out.println(e);
            throw new Exception(e);
        }
    }

}
