package com.example.lexicav1;

import android.os.Environment;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;

/**
 * Created by Cherry_Zhang on 2016-07-28.
 */
public class ExcelReader
{
    public static int insertExcelToSqlite(Database db, Sheet sheet) {

        int numImportErrors = 0;

        try {
            for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext(); ) {
                Row row = rit.next();

                row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
                row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_STRING);
                row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_NUMERIC);

                String word = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                String meaning = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                double isPassage = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();
                String fileURL = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

                if (word == null) {
                    word = ""; //will be caught by next if statement.
                }
                if (meaning == null) {
                    meaning = ""; //will be caught by next if statement.
                }
                if (fileURL == null) {
                    fileURL = "";
                }

                //TODO: don't know if I should scrap the following code, which saves locally
                //The image from the fileURL locally.
//                if (fileURL != null) {
//                    String fileName = UUID.randomUUID().toString() + ".jpg";
//                    //Save the image from file url locally.
//                    new ImageSaver(context).save(Uri.parse(fileURL), fileName, new ImageSaver.OnFileSavedListener()
//                    {
//                        @Override
//                        public void OnFileSaved()
//                        {
//                            fileURL = fileName;
//                        }
//                    });
//                }

                if (word.matches("") || meaning.matches("")
                        || Functions.isMadeOfSpaces(word)
                        || Functions.isMadeOfSpaces(meaning)
                        || db.itemExists(ItemListActivity.tableName, word))
                {
                    numImportErrors++;
                }
                else
                {
                    db.createItem(ItemListActivity.tableName, word, meaning, isPassage == 1 ? 1 : 0, fileURL);
                }
            }
        }
        catch (Exception e) {
            Log.e("ExcelReader", "error", e);
        }

        return numImportErrors;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
