package com.subsolr.index;

public final class IndexProperties {
   public final static int INDEXED = 0x00000001;
   public final static int TOKENIZED = 0x00000002;
   public final static int STORED = 0x00000004;
   public final static int BINARY = 0x00000008;
   public final static int OMIT_NORMS = 0x00000010;
   public final static int OMIT_TF_POSITIONS = 0x00000020;
   public final static int STORE_TERMVECTORS = 0x00000040;
   public final static int STORE_TERMPOSITIONS = 0x00000080;
   public final static int STORE_TERMOFFSETS = 0x00000100;

   public final static int MULTIVALUED = 0x00000200;
   public final static int SORT_MISSING_FIRST = 0x00000400;
   public final static int SORT_MISSING_LAST = 0x00000800;

   public final static int REQUIRED = 0x00001000;
   public final static int OMIT_POSITIONS = 0x00002000;

   public final static int STORE_OFFSETS = 0x00004000;
   public final static int DOC_VALUES = 0x00008000;
}
