package org.inspira.emag.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.capiz.bluetooth.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 2/01/16.
 */
public class RemueveElementosDeLista extends DialogFragment {

    private String[] elementos;
    private AccionDialogo ad;
    private List<Integer> elementosSeleccionados;

    public Integer[] getElementosSeleccionados() {
        return elementosSeleccionados.toArray(new Integer[0]);
    }

    public interface AccionDialogo extends Serializable {
        void accionPositiva(DialogFragment fragment);
        void accionNegativa(DialogFragment fragment);
    }

    public void setAd(AccionDialogo ad) {
        this.ad = ad;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle args;
        if(savedInstanceState == null){
            args = getArguments();
            elementosSeleccionados = new ArrayList<>();
        }else{
            args = savedInstanceState;
            elementosSeleccionados = args.getIntegerArrayList("elementos_seleccionados");
            ad = (AccionDialogo)args.getSerializable("acciones_dialogo");
        }
        elementos = args.getStringArray("elementos");
        boolean[] booleanos = new boolean[elementos.length];
        for(Integer elemento : elementosSeleccionados){
            booleanos[elemento] = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialogo_remocion_elementos_titulo)
                .setPositiveButton(R.string.dialogo_entrada_texto_aceptar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ad.accionPositiva(RemueveElementosDeLista.this);
                            }
                        })
                .setNegativeButton(R.string.dialogo_entrada_texto_cancelar,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ad.accionNegativa(RemueveElementosDeLista.this);
                            }
                        })
                .setMultiChoiceItems(elementos, booleanos, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            elementosSeleccionados.add(which);
                        }else{
                            elementosSeleccionados.remove(new Integer(which));
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putStringArrayList("elementos_seleccionados",(ArrayList)elementosSeleccionados);
        outState.putStringArray("elementos", elementos);
        outState.putSerializable("acciones_dialogo",ad);
    }
}