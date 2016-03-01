package org.inspira.emag.dialogos;

import android.support.design.widget.Snackbar;
import android.view.View;

import org.capiz.bluetooth.R;

/**
 * Created by Siempre on 19/02/2016.
 */
public class ProveedorSnackBar {

    public static void muestraBarraDeBocados(View view, String mensaje){
        Snackbar.make(view, mensaje, Snackbar.LENGTH_SHORT)
                .setAction(R.string.barra_de_bocados_aviso, null).show();
    }
}
