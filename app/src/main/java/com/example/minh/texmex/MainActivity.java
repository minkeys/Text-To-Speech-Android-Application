package com.example.minh.texmex;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.minh.texmex.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements OnItemClickListener {

    TextToSpeech ttsobject;     //text to speech Global object
    int validLanguage;          //validLanguage = ttsobject.setLanguage(Locale.ENGLISH);
    String text;                //text to be read, will extract et.toString()
    String smsMessageText;      //text variable in updateList
    //EditText et;                //text in textBox, will be converted to text with String text


    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();    //list of sms messages
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        //refreshSmsInbox();                                      //recreates query of sms messages
        //et = (EditText)findViewById(R.id.theTextTextbox);       //text in textbox

        //initialize TextToSpeech object on creation of main activity
        //OnInitListener interface must contain method to override
        ttsobject = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener(){

            @Override
            public void onInit(int status){
                if (status == TextToSpeech.SUCCESS){        //if status is a success
                    validLanguage = ttsobject.setLanguage(Locale.ENGLISH);  //Set Language Here
                    //validLanguage = ttsobject.setLanguage(Locale.CHINESE);
                }

                else {  //if status is not success
                        Toast.makeText(getApplicationContext(),     //handles error
                            "Feature not supported in your device.",
                            Toast.LENGTH_SHORT).show();             //show toast
                }
            }
        });

    }
/*
    public void onSpeakClick(){
        ttsobject.speak("Hello, I am talking to you.", TextToSpeech.QUEUE_FLUSH, null);
    }
*/
    public void textToVoice (){
        //if there is missing data or missing language
        if(validLanguage == TextToSpeech.LANG_NOT_SUPPORTED || validLanguage == TextToSpeech.LANG_MISSING_DATA){
            Toast.makeText(getApplicationContext(),         //handles error
                    "Feature not supported in your device.",
                    Toast.LENGTH_SHORT).show();             //show toast
        }

        else{
            //et.setText(smsMessageText);
            //text = et.getText().toString();
            text = smsMessageText;
            //flush out and update message with the text to be read
            ttsobject.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    //method that refreshes and recreates the query of sms messages
    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;     //if there are no messages or no more messages
        arrayAdapter.clear();
        do {
            String str = "SMS from: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
        smsMessageText = "new " + smsMessage.toString();        //put text in editText text to read
        this.textToVoice();                                     // call text to voice
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}