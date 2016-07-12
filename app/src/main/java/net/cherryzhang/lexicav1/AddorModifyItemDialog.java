package net.cherryzhang.lexicav1;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.cherryzhang.lexicav1.WordList.WordListBank;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Cherry_Zhang on 16-06-04.
 */
public class AddorModifyItemDialog
{
    final int SELECT_PHOTO = 900;
    private Database db;
    private Dialog dialog;
    private Context context;
    private LinearLayout layoutToHideWhenLoading;
    private ProgressBar progressCircle;
    private Button OKButton, getImageButton;
    private ImageView image, removeImage;
    private Uri imageUri = null;
    private EditText wordET;
    private EditText meaningET;
    private CheckBox checkBoxForIsPassage;
    private OnItemSuccessfullyAddedListener onItemSuccessfullyAddedListener;
    int dp100;
    public interface OnItemSuccessfullyAddedListener
    {
        //TODO: add image
        void OnItemSuccessfullyAdded(String wordText, String meaningText, boolean isPassageChecked);
    }

    public AddorModifyItemDialog(final Context context, final Database db, final WordListBank.WordItem existingWordToBeModified)
    {
        dp100 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
        this.context = context;
        this.db = db;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new Dialog(this.context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new Dialog(this.context);
        }
        dialog.setTitle("Create New Item");
        dialog.setContentView(R.layout.addworddialog);
        layoutToHideWhenLoading = (LinearLayout) dialog.findViewById(R.id.layoutToHideWhenLoading);
        progressCircle = (ProgressBar) dialog.findViewById(R.id.progressCircle);
        OKButton = (Button) dialog.findViewById(R.id.bOkForAddWordDialog);
        getImageButton = (Button) dialog.findViewById(R.id.bUploadImage);
        image = (ImageView) dialog.findViewById(R.id.imageView);
        removeImage = (ImageView) dialog.findViewById(R.id.removeImage);
        final TextView tvItem = (TextView) dialog.findViewById(R.id.tvEnterWord);
        final TextView tvDesc = (TextView) dialog.findViewById(R.id.tvEnterMeaning);
        wordET = (EditText) dialog
                .findViewById(R.id.etAddWord);
        meaningET = (EditText) dialog
                .findViewById(R.id.etAddMeaning);
        checkBoxForIsPassage = (CheckBox) dialog.findViewById(R.id.cbIsPassage);
        checkBoxForIsPassage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                tvItem.setText(isChecked ? "Enter Title of Passage:" : "Enter Item:");
                tvDesc.setText(isChecked ? "Enter Body of Passage" : "Enter Hint for Item:");
            }
        });

        if (existingWordToBeModified != null)
        {
            dialog.setTitle("Edit Item");
            OKButton.setText("Edit");
            getImageButton.setText("Edit Image");
            wordET.setText(existingWordToBeModified.item);
            meaningET.setText(existingWordToBeModified.description);
            checkBoxForIsPassage.setChecked(existingWordToBeModified.isPassage == 1);
            if (existingWordToBeModified.imageFileName != null)
            {
                if (!existingWordToBeModified.imageFileName.contentEquals(""))
                {
                    File imageFile = new ImageSaver(context).load(existingWordToBeModified.imageFileName);
                    imageUri = Uri.fromFile(imageFile);
                    Picasso.with(context)
                            .load(imageFile)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .resize(dp100, dp100).centerInside()
                            .into(image, new Callback()
                            {
                                @Override
                                public void onSuccess()
                                {
                                    Log.w("AddorModifyDialog", "onSuccessInitialLoadingOfImage");
                                }

                                @Override
                                public void onError()
                                {
                                    Log.w("AddorModifyDialog", "onErrorInitialLoadingOfImage");
                                }
                            });
                }
                else
                {
                    Log.w("AddorModifyDialog", "imageFileName ''");
                }
            }
            else
            {
                Log.w("AddorModifyDialog", "imageFileName null");
            }
        }

        getImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                ((Activity)context).startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        removeImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                imageUri = null;
                image.setImageBitmap(null);
                image.invalidate();
            }
        });

        OKButton.setSoundEffectsEnabled(false);
        OKButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //hide the contents of this dialog box and display a loading progress circle instead
                layoutToHideWhenLoading.setVisibility(View.INVISIBLE);
                progressCircle.setVisibility(View.VISIBLE);

                String word = wordET.getText().toString();
                String meaning = meaningET.getText().toString();
                boolean isPassageChecked = checkBoxForIsPassage.isChecked();
                db.open();
                if (existingWordToBeModified == null)
                {
                    if (word.matches("") || meaning.matches("")
                            || Functions.isMadeOfSpaces(word)
                            || Functions.isMadeOfSpaces(meaning)
                            || db.itemExists(ItemListActivity.tableName, word))
                    {
                        dismissDialogDueToInvalidWordOrMeaning();
                    }
                    else
                    {
                        addItemIntoDatabase(word, meaning, isPassageChecked, null);
                    }
                }
                else
                {
                    if (word.matches("") || meaning.matches("")
                            || Functions.isMadeOfSpaces(word)
                            || Functions.isMadeOfSpaces(meaning)
                            || db.itemExists(ItemListActivity.tableName, word) && !word.equals(existingWordToBeModified.item))
                    {
                        dismissDialogDueToInvalidWordOrMeaning();
                    }
                    else
                    {
                        addItemIntoDatabase(word, meaning, isPassageChecked, existingWordToBeModified);
                    }
                }
            }
        });
    }

    private void addItemIntoDatabase(final String word, final String meaning, final boolean isPassageChecked, final WordListBank.WordItem existingWordToBeModified)
    {
        String fileName = UUID.randomUUID().toString() + ".jpg";

        if (existingWordToBeModified == null) //in createItem mode
        {
            if (imageUri != null)
            {
                //save file
                db.createItem(ItemListActivity.tableName, word, meaning, isPassageChecked ? 1 : 0, fileName);
                saveImageFile(fileName);
            }
            else
            {
                //no image file to save, so save empty string in database
                db.createItem(ItemListActivity.tableName, word, meaning, isPassageChecked ? 1 : 0, "");
                updateListOfItemsAndCloseDialog();
            }
        }
        else //in editExistingItem mode
        {
            if (existingWordToBeModified.imageFileName != null && imageUri != null)
            {
                File imageFile = new ImageSaver(context).load(existingWordToBeModified.imageFileName);
                Uri uriFromOriginalItem = Uri.fromFile(imageFile);
                if (uriFromOriginalItem.compareTo(imageUri) == 0) //if the image has not been edited
                {
                    //save new file with the same image as before
                    db.editItem(ItemListActivity.tableName, existingWordToBeModified, word, meaning, isPassageChecked ? 1 : 0, existingWordToBeModified.imageFileName);
                    updateListOfItemsAndCloseDialog();
                }
                else
                {
                    //delete old file
                    String oldImageFileName = existingWordToBeModified.imageFileName;
                    boolean deleted = new ImageSaver(context).deleteFile(oldImageFileName);
                    Log.w("image file deleted??", "" + deleted);
                    Log.w("imageFileToBeDeleted", "" + oldImageFileName);

                    //save new file
                    db.editItem(ItemListActivity.tableName, existingWordToBeModified, word, meaning, isPassageChecked ? 1 : 0, fileName);
                    saveImageFile(fileName);
                }
            }
            else
            {
                if (imageUri != null)
                {
                    //save file
                    db.editItem(ItemListActivity.tableName, existingWordToBeModified, word, meaning, isPassageChecked ? 1 : 0, fileName);
                    saveImageFile(fileName);
                }
                else
                {
                    //no image file to save, so save empty string in database
                    db.editItem(ItemListActivity.tableName, existingWordToBeModified, word, meaning, isPassageChecked ? 1 : 0, "");
                    updateListOfItemsAndCloseDialog();
                }
            }
        }
    }

    private void saveImageFile(final String fileName)
    {
        new ImageSaver(context).save(imageUri, fileName, new ImageSaver.OnFileSavedListener()
        {
            @Override
            public void OnFileSaved()
            {
                updateListOfItemsAndCloseDialog();
            }
        });
    }

    private void updateListOfItemsAndCloseDialog()
    {
        int order = Settings.getOrderFromSP(context, ItemListActivity.tableName);
        ArrayList<WordListBank.WordItem> items = db
                .getDataItems(ItemListActivity.tableName, order);
        WordListBank.setContext(items);
        ItemListFragment.aa.notifyDataSetChanged();

        image.setImageBitmap(null);
        image.invalidate();
        dialog.dismiss();
        onItemSuccessfullyAddedListener.OnItemSuccessfullyAdded(wordET.getText().toString(), meaningET.getText().toString(), checkBoxForIsPassage.isChecked());
        db.close();
    }

    private void dismissDialogDueToInvalidWordOrMeaning()
    {
        dialog.dismiss();
        db.close();
        Toast.makeText(context,
                "Invalid word and/or meaning input(s).", Toast.LENGTH_SHORT)
                .show();
    }

    public void startDialog(OnItemSuccessfullyAddedListener onItemSuccessfullyAddedListener)
    {
        dialog.show();
        this.onItemSuccessfullyAddedListener = onItemSuccessfullyAddedListener;
    }

    public void setDatabase(Database db)
    {
        this.db = db;
    }

    //This method must be called in the activity that is instantiating this class
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent)
    {
        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == Activity.RESULT_OK){
                    imageUri = imageReturnedIntent.getData();
                    Picasso.with(context).load(imageUri).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).resize(dp100, dp100).centerInside().into(image);
                }
                break;
        }
    }
}
