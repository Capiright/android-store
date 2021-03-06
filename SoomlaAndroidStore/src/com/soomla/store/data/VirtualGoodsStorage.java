/*
 * Copyright (C) 2012 Soomla Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soomla.store.data;

import android.database.Cursor;
import android.util.Log;
import com.soomla.billing.util.AESObfuscator;
import com.soomla.store.StoreConfig;
import com.soomla.store.domain.data.VirtualGood;

/**
 * This class provide basic storage operations on VirtualGoods.
 */
public class VirtualGoodsStorage {

    /** Constructor
     *
     */
    public VirtualGoodsStorage() {
    }


    /** Public functions **/

    /**
     * Fetch the balance of the given virtual good.
     * @param virtualGood is the required virtual good.
     * @return the balance of the required virtual currency.
     */
    public int getBalance(VirtualGood virtualGood){
        if (StoreConfig.debug){
            Log.d(TAG, "trying to fetch balance for virtual good with itemId: " + virtualGood.getItemId());
        }
        String itemId = virtualGood.getItemId();
        if (StorageManager.getInstance().getObfuscator() != null){
            itemId = StorageManager.getInstance().getObfuscator().obfuscateString(itemId);
        }
        Cursor cursor = StorageManager.getInstance().getDatabase().getVirtualGood(itemId);

        if (cursor == null) {
            return 0;
        }

        try {
            int balanceCol = cursor.getColumnIndexOrThrow(
                    StoreDatabase.VIRTUAL_GOODS_COLUMN_BALANCE);
            if (cursor.moveToNext()) {
                String balanceStr = cursor.getString(balanceCol);
                int balance;
                if (StorageManager.getInstance().getObfuscator() != null){
                    balance = StorageManager.getInstance().getObfuscator().unobfuscateToInt(balanceStr);
                }
                else {
                    balance = Integer.parseInt(balanceStr);
                }

                if (StoreConfig.debug){
                    Log.d(TAG, "the balance for " + virtualGood.getItemId() + " is " + balance);
                }
                return balance;
            }
        } catch (AESObfuscator.ValidationException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return 0;
	}



    /**
    * Adds the given amount of goods to the storage.
    * @param virtualGood is the required virtual good.
    * @param amount is the amount of goods to add.
    */
    public int add(VirtualGood virtualGood, int amount){
        if (StoreConfig.debug){
            Log.d(TAG, "adding " + amount + " " + virtualGood.getName() + ".");
        }

        String itemId = virtualGood.getItemId();
        int balance = getBalance(virtualGood);
        String quantityStr = "" + (balance + amount);
        if (StorageManager.getInstance().getObfuscator() != null){
            quantityStr = StorageManager.getInstance().getObfuscator().obfuscateString(quantityStr);
            itemId      = StorageManager.getInstance().getObfuscator().obfuscateString(itemId);
        }
        StorageManager.getInstance().getDatabase().updateVirtualGood(itemId, quantityStr);

        return balance + amount;
	}

    /**
     * Removes the given amount from the given virtual good's balance.
     * @param virtualGood is the virtual good to remove the given amount from.
     * @param amount is the amount to remove.
     */
    public int remove(VirtualGood virtualGood, int amount){
        if (StoreConfig.debug){
            Log.d(TAG, "removing " + amount + " " + virtualGood.getName() + ".");
        }

        String itemId = virtualGood.getItemId();
        int quantity = getBalance(virtualGood) - amount;
        quantity = quantity > 0 ? quantity : 0;
        String quantityStr = "" + quantity;
        if (StorageManager.getInstance().getObfuscator() != null){
            quantityStr = StorageManager.getInstance().getObfuscator().obfuscateString(quantityStr);
            itemId      = StorageManager.getInstance().getObfuscator().obfuscateString(itemId);
        }
        StorageManager.getInstance().getDatabase().updateVirtualGood(itemId, quantityStr);

        return quantity;
	}

    /** Private members **/
    private static final String TAG = "SOOMLA VirtualGoodsStorage";
}
