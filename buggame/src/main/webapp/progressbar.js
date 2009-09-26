function progressbar(progress) {
    if ( progress == null ) {
        return null;
    }

    var html = "<table width='100%' cellspacing='1' cellpadding='0' border='0'>";
    html += "<tr>";

    for(var ix = 0; ix < 100; ix=ix + 10)
    {
        if(ix < progress) {
            html += "<td class='green' nowrap='nowrap'>&nbsp;</td>";
        } else {
            html += "<td class='red' nowrap='nowrap'>&nbsp;</td>";
        }
    }

    html += "</tr>";
    html += "</table>";
    return html;
}