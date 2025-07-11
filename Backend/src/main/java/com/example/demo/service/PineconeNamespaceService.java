package com.example.demo.service;

import com.example.demo.entity.ColumnInfo;
import com.example.demo.entity.TableInfo;
import io.pinecone.clients.Index;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PineconeNamespaceService {
    @Autowired
    private Index index;

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(", ", list);
    }

    public void upsertTableRecord(TableInfo table, String username, ObjectId datbaseconfigid){
        try {
            ArrayList<Map<String, String>> upsertRecords = new ArrayList<>();

            Map<String, String> record = new HashMap<>();
            record.put("_id", table.getId().toString());
            record.put("name", table.getTableName());
            record.put("chunk_text", table.getTableName() + " : " + table.getDescription());
            record.put("joinsWith", listToString(table.getJoinsWith()));
            record.put("primaryKeys", listToString(table.getPrimaryKeys()));
            record.put("foreignKeys", listToString(table.getForeignKeys()));

            upsertRecords.add(record);

            String namespace = username+"-"+(datbaseconfigid.toString())+"-tabledetails";

            index.upsertRecords(namespace, upsertRecords);
        } catch (Exception e) {
            System.out.println("Something went wrong while uploading table information......");
            throw new RuntimeException(e);
        }
    }

    public void deleteTableRecord(ObjectId tableid, String username, ObjectId datbaseconfigid){
        List<String> ids = new ArrayList<>();
        ids.add(tableid.toString());

        String namespace = username+"-"+(datbaseconfigid.toString())+"-tabledetails";
        index.deleteByIds(ids, namespace);
    }

    public void upsertColumnRecord(ColumnInfo column, String username, ObjectId datbaseconfigid){
        try {
            ArrayList<Map<String, String>> upsertRecords = new ArrayList<>();

            Map<String, String> record = new HashMap<>();

            String chunkdata = "ColumnName : " + column.getColumnName() + " # " + "TableName : " + column.getTableName() +
                    " # " + "Description : " + column.getDescription() + " # " + "Type : " + column.getType() +
                    " # " + "ForeignRelation : " + column.getForeignRelation();

            record.put("_id", column.getId().toString());
            record.put("chunk_text", chunkdata);
            upsertRecords.add(record);

            String namespace = username+"-"+(datbaseconfigid.toString())+"-columnDetails";

            index.upsertRecords(namespace, upsertRecords);
        } catch (Exception e) {
            System.out.println("Something went wrong while uploading column information......");
            throw new RuntimeException(e);
        }
    }

    public void deleteColumnRecord(ObjectId columnid, String username, ObjectId datbaseconfigid){
        List<String> ids = new ArrayList<>();
        ids.add(columnid.toString());

        String namespace = username+"-"+(datbaseconfigid.toString())+"-columnDetails";
        index.deleteByIds(ids, namespace);
    }
}
