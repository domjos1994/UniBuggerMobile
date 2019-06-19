/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackermobile.R;

public class ArrayHelper {

    @SuppressLint("ResourceType")
    public static List<String> getValues(Context context, String key) {
        List<String> values = new LinkedList<>();
        List<TypedArray> typedArrays = ArrayHelper.getMultiTypedArray(context, key);
        for (int i = 0; i <= typedArrays.size() - 1; i++) {
            values.add(typedArrays.get(i).getString(1));
        }
        return values;
    }

    public static int getIdOfEnum(Context context, Spinner spinner, String key) {
        int position = spinner.getSelectedItemPosition();
        List<TypedArray> typedArrays = ArrayHelper.getMultiTypedArray(context, key);
        for (int i = 0; i <= typedArrays.size() - 1; i++) {
            if (position == i) {
                return typedArrays.get(i).getInt(0, 0);
            }
        }
        return 0;
    }

    public static void setValueOfEnum(Context context, int id, String key, Spinner spinner) {
        List<TypedArray> typedArrays = ArrayHelper.getMultiTypedArray(context, key);
        for (int i = 0; i <= typedArrays.size() - 1; i++) {
            if (id == typedArrays.get(i).getInt(0, 0)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private static List<TypedArray> getMultiTypedArray(Context context, String key) {
        List<TypedArray> array = new ArrayList<>();

        try {
            Class<R.array> res = R.array.class;
            Field field;
            int counter = 0;

            do {
                field = res.getField(key + "_" + counter);
                array.add(context.getResources().obtainTypedArray(field.getInt(null)));
                counter++;
            } while (true);
        } catch (Exception e) {
            return array;
        }
    }
}