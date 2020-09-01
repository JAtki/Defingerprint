package com.example.defingerprint;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.Telephony;

import java.util.Random;

/**
 * Generates pseduo-random SMS data and inserts it into the SMS Inbox, Sent,
 * and Draft tables with a weighted random distribution.
 */
public class GenerateSMSRunnable implements Runnable
{
    private int operations;
    private Context activityContext;

    /**
     * Constructor.
     * @param iterations The number of messages to be generated.
     * @param context The Context used to access a ContentProvider
     */
    public GenerateSMSRunnable(int iterations, Context context)
    {
        operations = iterations;
        activityContext = context;
    }

    @Override
    public void run() {
        Random rand = new Random();
        // if no seed provided

        long seed = rand.nextLong();
        rand.setSeed(seed);

        //create "address" of phone numbers
        String[] phonebook = new String[rand.nextInt((operations/4)+2)];

        for (int i = 0; i < phonebook.length; i++)
        {
            //begin with 07, append with 9 digits
            phonebook[i] = "07";
            for (int j = 0; j < 9; j++)
                phonebook[i] = phonebook[i] + rand.nextInt(9);
        }

        for (int opNo = 0; opNo < operations; opNo++) {


            //generate a random message body
            int wordCount = rand.nextInt(18)+2;

            String msg = "";
            for (int wordI = 0; wordI < wordCount; wordI++)
            {
                int wordLen = rand.nextInt(6) + 2;
                char[] thisWord = new char[wordLen];

                for (int wordPos = 0; wordPos < wordLen; wordPos++)
                    thisWord[wordPos] = (char)('a' + rand.nextInt(26));

                if (wordI + 1 < wordCount)
                    msg = msg + new String(thisWord) + " ";
                else
                    msg = msg + new String(thisWord) + ".";

            }

            //choose a phone number from phoneBook
            String phoneNumber = phonebook[rand.nextInt(phonebook.length)];

            //generate random dates, limit to this month
            long monthStart = System.currentTimeMillis();
            long msgDate = (monthStart - (rand.nextLong()%2629800000L));


            //swap between Inbox and sent, and then for some amount of final messages, write drafts
            int decideMsgType = rand.nextInt(50);
            Uri msgURI;
            if (decideMsgType == 0)
                msgURI = Telephony.Sms.Draft.CONTENT_URI;
            else if (decideMsgType%2 == 1)
                msgURI = Telephony.Sms.Sent.CONTENT_URI;
            else
                msgURI = Telephony.Sms.Inbox.CONTENT_URI;

            //insert generated values
            try {
                ContentValues values = new ContentValues();
                values.put(Telephony.TextBasedSmsColumns.ADDRESS, phoneNumber);
                values.put(Telephony.TextBasedSmsColumns.BODY, msg);
                values.put(Telephony.TextBasedSmsColumns.READ, 1);
                values.put(Telephony.TextBasedSmsColumns.DATE, msgDate);
                values.put(Telephony.TextBasedSmsColumns.DATE_SENT, msgDate);
                activityContext.getContentResolver().insert(msgURI, values);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
