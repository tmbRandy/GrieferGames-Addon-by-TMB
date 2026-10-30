package tmb.randy.tmbgriefergames.core.flow;

/** A line in the editor: the output {@code output} of node {@code from} leads to node {@code to}. */
public record FlowConnection(String from, String output, String to) {}
