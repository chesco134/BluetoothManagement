package org.inspira.emag.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.capiz.bluetooth.R;

/**
 * Created by jcapiz on 31/12/15.
 */
public class Informacion extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String mensaje = getArguments().getString("mensaje");
        builder.setIcon(R.drawable.ic_info_black_24dp);
        builder.setTitle("Información");
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar",null);
        return builder.create();
    }
}