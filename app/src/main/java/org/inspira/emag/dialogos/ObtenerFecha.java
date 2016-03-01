package org.inspira.emag.dialogos;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jcapiz on 2/01/16.
 */
public class ObtenerFecha extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion;
    private Date fecha;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
        dpd.setTitle("Definir fecha");
        return dpd;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        fecha = calendar.getTime();
        agenteDeInteraccion.clickSobreAccionPositiva(ObtenerFecha.this);
    }

    public void setAgenteDeInteraccion(DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion) {
        this.agenteDeInteraccion = agenteDeInteraccion;
    }

    public Date getFecha() {
        return fecha;
    }
}
